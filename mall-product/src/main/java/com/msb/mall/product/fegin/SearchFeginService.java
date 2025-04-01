package com.msb.mall.product.fegin;

import com.msb.common.dto.es.SkuESModel;
import com.msb.common.utils.R;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.List;

@FeignClient("mall-search")
public interface SearchFeginService {

    @PostMapping("/search/save/product")
    public R productStatusUp(@RequestBody List<SkuESModel> skuESModels);
}


