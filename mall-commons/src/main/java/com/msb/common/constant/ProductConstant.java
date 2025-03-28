package com.msb.common.constant;

/**
 * 商品模块的常量
 */
public class ProductConstant {
   /* // 基本属性
    public static final Integer ATTR_TYPE_BASE = 1;
    // 销售属性
    public static final Integer ATTR_TYPE_SALE = 0;*/
    public enum AttrEnum  {
        ATTR_TYPE_BASE(1,"基本属性"),ATTR_TYPE_SALE(0,"销售属性");
        private int code;
        private String msg;
        AttrEnum(int code,String msg){
            this.code = code;
            this.msg = msg;
        }
        
        public  int getCode(){
            return  code;
        }

       public String getMsg() {
           return msg;
       }
   }

    public enum StatusEnum{
        SPU_NEW(0,"新建"),
        SPU_UP(1,"上架"),
        SPU_DOWN(2,"下架");
        private int code;
        private String msg;
        StatusEnum(int code,String msg){
            this.code = code;
            this.msg = msg;
        }
        public int getCode(){
            return  code;
        }

        public String getMsg() {
            return msg;
        }
    }

}
