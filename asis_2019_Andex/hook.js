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

    // var classLoaderToUse = Java.enumerateClassLoadersSync()[1]; //Get another classloader
    // Java.classFactory.loader = classLoaderToUse; //Set the classloader to the correct one
    // var res = classLoaderToUse.findClass('<class name>'); //Just some simple test to make sure that the class can be loaded
    // console.log(res);


    // Java.use('java.lang.reflect.Method').invoke.overload('java.lang.Object', '[Ljava.lang.Object;', 'boolean').implementation = function(a,b,c) {
    //     console.log('hooked!', a, b, c);
    //     return this.invoke(a,b,c);
    // };



    // Java.perform(function y(){ 
    //     console.log("Inside java perform function");
    //     var my_class = Java.use("com.asisctf.config.SayHelloToYourLittleFriend.config");
    //     // my_class.config.implementation = function(x){
    //     // console.log( "original call"+x);
    //     // }
    
    //         console.log( "original call"+my_class);

    // });

    // var dex = Java.use("java.lang.reflect.Method");
    // dex.invoke.overload("java.lang.Object",'[Ljava.lang.Object;').implementation = function(x,y){