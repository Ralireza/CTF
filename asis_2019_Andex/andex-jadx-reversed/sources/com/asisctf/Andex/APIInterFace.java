package com.asisctf.Andex;

import com.asisctf.Andex.models.ConfigModel;
import com.asisctf.Andex.models.RegisterationModel;
import com.asisctf.Andex.models.ShopItemsModel;
import com.asisctf.Andex.models.ShopOrderModel;
import com.asisctf.Andex.models.UpdateReq;
import com.asisctf.Andex.models.UpdateUserProfileModel;
import com.asisctf.Andex.models.UserProfileModel;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.POST;
import retrofit2.http.Path;
import retrofit2.http.Url;

public interface APIInterFace {
    @POST
    Call<UpdateUserProfileModel> PostUserProf(@Header("uuid") String str, @Header("checksum") String str2, @Url String str3, @Body UpdateReq updateReq);

    @GET("api/get_config/{rolid}")
    Call<ConfigModel> getConf(@Header("uuid") String str, @Path("rolid") String str2);

    @GET("api/get_dex/{dex}")
    Call<ResponseBody> getDex(@Header("uuid") String str, @Path("dex") String str2);

    @GET("/api/userClass/register/{name}")
    Call<RegisterationModel> getReg(@Path("name") String str);

    @GET
    Call<ShopItemsModel> getShopItem(@Header("uuid") String str, @Url String str2);

    @GET
    Call<ShopOrderModel> getShopOrder(@Header("uuid") String str, @Url String str2);

    @GET
    Call<ResponseBody> getShopOrderD(@Header("uuid") String str, @Url String str2);

    @GET
    Call<UserProfileModel> getUserProf(@Header("uuid") String str, @Url String str2);
}
