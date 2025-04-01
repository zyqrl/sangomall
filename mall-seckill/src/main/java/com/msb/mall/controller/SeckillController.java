package com.msb.mall.controller;

import com.alibaba.fastjson.JSON;
import com.msb.common.utils.R;
import com.msb.mall.dto.SeckillSkuRedisDto;
import com.msb.mall.service.SeckillService;
import org.apache.skywalking.apm.toolkit.trace.Trace;
import org.apache.skywalking.apm.toolkit.trace.TraceContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.annotation.AliasFor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;
@Controller
@RequestMapping("/seckill")
public class SeckillController {

    @Autowired
    SeckillService seckillService;

    @GetMapping("/currentSeckillSessionSkus")
    @ResponseBody
    public R getCurrentSeckillSessionSkus(){
        List<SeckillSkuRedisDto> currentSeckillSkus = seckillService.getCurrentSeckillSkus();

        return R.ok().put("data", JSON.toJSONString(currentSeckillSkus));
    }

    @Trace
    @ResponseBody
    @GetMapping("/seckillSessionBySkuId")
    public R getSeckillSessionBySkuId(@RequestParam("skuId") Long skuId){
        System.out.println("seckillSessionBySkuId -----------------------"+ TraceContext.traceId());
        try {
            Thread.sleep(500);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        SeckillSkuRedisDto dto = seckillService.getSeckillSessionBySkuId(skuId);
        return  R.ok().put("data",JSON.toJSONString(dto));
    }

    /**
     * 秒杀抢购
     * killId=1_9&code=69d55333c9ec422381024d34fdfd3e85&num=1
     * @return
     */
    @GetMapping("/kill")
    public String seckill(@RequestParam("killId") String killId,
                          @RequestParam("code") String code,
                          @RequestParam("num") Integer num,
                          Model model){
        String orderSN = seckillService.kill(killId,code,num);
        model.addAttribute("orderSn",orderSN);
        return "success";
    }
}
