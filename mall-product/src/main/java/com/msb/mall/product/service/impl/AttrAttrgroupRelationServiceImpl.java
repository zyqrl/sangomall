package com.msb.mall.product.service.impl;

import com.msb.mall.product.vo.AttrGroupRelationVO;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.msb.common.utils.PageUtils;
import com.msb.common.utils.Query;

import com.msb.mall.product.dao.AttrAttrgroupRelationDao;
import com.msb.mall.product.entity.AttrAttrgroupRelationEntity;
import com.msb.mall.product.service.AttrAttrgroupRelationService;


@Service("attrAttrgroupRelationService")
public class AttrAttrgroupRelationServiceImpl extends ServiceImpl<AttrAttrgroupRelationDao, AttrAttrgroupRelationEntity> implements AttrAttrgroupRelationService {

    @Override
    public PageUtils queryPage(Map<String, Object> params) {
        IPage<AttrAttrgroupRelationEntity> page = this.page(
                new Query<AttrAttrgroupRelationEntity>().getPage(params),
                new QueryWrapper<AttrAttrgroupRelationEntity>()
        );

        return new PageUtils(page);
    }

    @Override
    public void saveBatch(List<AttrGroupRelationVO> vos) {
        List<AttrAttrgroupRelationEntity> collect = vos.stream().map((m) -> {
            AttrAttrgroupRelationEntity entity = new AttrAttrgroupRelationEntity();
            BeanUtils.copyProperties(m, entity);
            return entity;
        }).collect(Collectors.toList());
        this.saveBatch(collect);
    }
}