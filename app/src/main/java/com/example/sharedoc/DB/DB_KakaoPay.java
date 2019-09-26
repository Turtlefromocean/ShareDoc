package com.example.sharedoc.DB;

public class DB_KakaoPay {


    public KakaoPay kakaoPay = new KakaoPay();

    public class KakaoPay {
        public String cid;
        public String partner_order_id;
        public String partner_user_id;
        public String item_name;
        public Integer quantity;
        public Integer total_amount;
        public Integer tax_free_amount;
        public String approval_url;
        public String cancel_url;
        public String fail_url;
    }
}
