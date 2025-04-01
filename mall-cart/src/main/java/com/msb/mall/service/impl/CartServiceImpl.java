package com.msb.mall.service.impl;

import com.alibaba.fastjson.JSON;
import com.msb.common.constant.CartConstant;
import com.msb.common.utils.R;
import com.msb.common.vo.MemberVO;
import com.msb.mall.Interceptor.AuthInterceptor;
import com.msb.mall.feign.ProductFeignService;
import com.msb.mall.service.ICartService;
import com.msb.mall.vo.Cart;
import com.msb.mall.vo.CartItem;
import com.msb.mall.vo.SkuInfoVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.BoundHashOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.stream.Collectors;

/**
 * 购物车信息是存储在Redis中的
 */
@Service
public class CartServiceImpl implements ICartService {

    @Autowired
    StringRedisTemplate redisTemplate;

    @Autowired
    ProductFeignService productFeignService;

    @Autowired
    ThreadPoolExecutor executor;

    /**
     * 查询出当前登录用户的所有的购物车信息
     * @return
     */
    @Override
    public Cart getCartList() {
        BoundHashOperations<String, Object, Object> operations = getCartKeyOperation();
        Set<Object> keys = operations.keys();
        Cart cart = new Cart();
        List<CartItem> list = new ArrayList<>();
        for (Object k : keys) {
            String key = (String) k;
            Object o = operations.get(key);
            String json = (String) o;
            CartItem item = JSON.parseObject(json, CartItem.class);
            list.add(item);
        }
        cart.setItems(list);
        return cart;
    }

    /**
     * 把商品添加到购物车中
     * @param skuId
     * @param num
     * @return
     */
    @Override
    public CartItem addCart(Long skuId, Integer num) throws Exception {
        BoundHashOperations<String, Object, Object> hashOperations = getCartKeyOperation();
        // 如果Redis存储在商品的信息，那么我们只需要修改商品的数量就可以了
        Object o = hashOperations.get(skuId.toString());
        if(o != null){
            // 说明已经存在了这个商品那么修改商品的数量即可
            String json = (String) o;
            CartItem item = JSON.parseObject(json, CartItem.class);
            item.setCount(item.getCount()+num);
            hashOperations.put(skuId.toString(),JSON.toJSONString(item));
            return item;
        }
        CartItem item = new CartItem();
        CompletableFuture future1 = CompletableFuture.runAsync(()->{
            // 1.远程调用获取 商品信息
            R r = productFeignService.info(skuId);
            String skuInfoJSON = (String) r.get("skuInfoJSON");
            SkuInfoVo vo = JSON.parseObject(skuInfoJSON,SkuInfoVo.class);
            item.setCheck(true);
            item.setCount(num);
            item.setPrice(vo.getPrice());
            item.setImage(vo.getSkuDefaultImg());
            item.setSkuId(skuId);
            item.setTitle(vo.getSkuTitle());
            item.setSpuId(vo.getSpuId());
        },executor);

        CompletableFuture future2 = CompletableFuture.runAsync(()->{
            // 2.获取商品的销售属性
            List<String> skuSaleAttrs = productFeignService.getSkuSaleAttrs(skuId);
            item.setSkuAttr(skuSaleAttrs);
        },executor);

        CompletableFuture.allOf(future1,future2).get();
        // 3.把数据存储在Redis中
        String json = JSON.toJSONString(item);
        hashOperations.put(skuId.toString(),json);

        return item;
    }

    @Override
    public CartItem getCartItem(Long skuId) {
        BoundHashOperations<String, Object, Object> operations = getCartKeyOperation();

        Object o = operations.get(skuId.toString());
        String json = (String) o;
        CartItem item = JSON.parseObject(json, CartItem.class);
        return item;
    }

    /**
     * 获取当前登录用户选中的商品信息 购物车中
     * @return
     */
    @Override
    public List<CartItem> getUserCartItems() {
        BoundHashOperations<String, Object, Object> operations = getCartKeyOperation();
        List<Object> values = operations.values();
        List<CartItem> list = values.stream().map(item -> {
            String json = (String) item;
            CartItem cartItem = JSON.parseObject(json, CartItem.class);
            return cartItem;
        }).filter(item -> {
            return item.isCheck();
        }).collect(Collectors.toList());
        return list;
    }

    private BoundHashOperations<String, Object, Object> getCartKeyOperation() {
        // hash key: cart:1   skuId:cartItem
        MemberVO memberVO = AuthInterceptor.threadLocal.get();
        String cartKey = CartConstant.CART_PERFIX + memberVO.getId();
        BoundHashOperations<String, Object, Object> hashOperations = redisTemplate.boundHashOps(cartKey);
        return hashOperations;
    }
}
