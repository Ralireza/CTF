console.log("Script loaded successfully ");
Java.perform(function x(){ 
    console.log("Inside java perform function");
    var my_class = Java.use("com.asisctf.Andex.ConfigurationActivity");
    
        my_class.getDex1.implementation = function(x){

            var someClass = Java.use("com.asisctf.Andex.Utils");

            someClass.user_profile_url.value="/api/userClass/me";
            someClass.shop_order_url.value="/api/shop/order/";
            someClass.shop_item_url.value="/api/shop/items/get_data";

            var intent=Java.use("android.content.Intent");
            var c=Java.use("com.asisctf.Andex.MenuActivity");
            var result = intent.$new(this, c.class);
           

            this.startActivity(result);


       
    }});

   
