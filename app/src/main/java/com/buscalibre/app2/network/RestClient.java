package com.buscalibre.app2.network;

import com.buscalibre.app2.constants.AppConstants;
import com.buscalibre.app2.models.CartResult;
import com.buscalibre.app2.models.ChechEmail;
import com.buscalibre.app2.models.CurrentPayment;
import com.buscalibre.app2.models.Ebook;
import com.buscalibre.app2.models.ForgotPass;
import com.buscalibre.app2.models.InboxMessages;
import com.buscalibre.app2.models.NewPaymentMethod;
import com.buscalibre.app2.models.OrderDetail;
import com.buscalibre.app2.models.PaymentDetail;
import com.buscalibre.app2.models.PaymentHistory;
import com.buscalibre.app2.models.QuoteResult;
import com.buscalibre.app2.models.SellerConditions;
import com.buscalibre.app2.models.SellerInfo;
import com.buscalibre.app2.models.SellerOrder;
import com.buscalibre.app2.models.SellerQuote;
import com.buscalibre.app2.models.SellerShowcase;
import com.buscalibre.app2.models.SellerSuccess;
import com.buscalibre.app2.models.ServerCountries;
import com.buscalibre.app2.models.ServerMenuOptions;
import com.buscalibre.app2.models.ServerStores;
import com.buscalibre.app2.models.StandarResponse;
import com.buscalibre.app2.models.SystemConfig;
import com.buscalibre.app2.models.TokenUpdated;
import com.buscalibre.app2.models.UserCart;
import com.buscalibre.app2.models.UserLogin;
import com.buscalibre.app2.models.UserPayments;
import com.buscalibre.app2.models.UserRegister;
import com.google.gson.JsonObject;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.DELETE;
import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.Headers;
import retrofit2.http.PATCH;
import retrofit2.http.POST;
import retrofit2.http.Path;
import retrofit2.http.Query;

public interface RestClient {

    @Headers("Content-Type: application/json")
    @POST("v1/users/check_mail")
    Call<ChechEmail> checkEmail(@Body JsonObject body);

    @Headers("Content-Type: application/json")
    @POST("v1/users/login")
    Call<UserLogin> initSession(@Body JsonObject body);

    @Headers("Content-Type: application/json")
    @POST("v1/users/forgot_pass")
    Call<ForgotPass> forgotPass(@Body JsonObject body);

    @Headers("Content-Type: application/json")
    @POST("v1/users/register")
    Call<UserRegister> registerUser(@Body JsonObject body);

    @Headers("Content-Type: application/json")
    @PATCH("v1/users/firebase")
    Call<TokenUpdated> renewFirebaseToken(@Header(AppConstants.TOKEN_CONSTANT) String token,
                                          @Header(AppConstants.LOCALE_CONSTANT) String locale,
                                          @Body JsonObject body);

    @Headers("Content-Type: application/json")
    @GET("v1/config/countries")
    Call<ServerCountries> getServerCountries(@Header(AppConstants.TOKEN_CONSTANT) String token,
                                    @Header(AppConstants.LOCALE_CONSTANT) String locale);

    @Headers("Content-Type: application/json")
    @GET("v1/config/country_options")
    Call<ServerMenuOptions> getServerMenuOptions(@Header(AppConstants.TOKEN_CONSTANT) String token,
                                               @Header(AppConstants.LOCALE_CONSTANT) String locale,
                                               @Query("c") String countryID);

    @Headers("Content-Type: application/json")
    @GET("v1/config/stores")
    Call<ServerStores> getServerStores(@Header(AppConstants.TOKEN_CONSTANT) String token,
                                       @Header(AppConstants.LOCALE_CONSTANT) String locale,
                                       @Query("c") String countryID);

    @Headers("Content-Type: application/json")
    @PATCH("v1/users/country")
    Call<StandarResponse> setSelectedCountry(@Header(AppConstants.TOKEN_CONSTANT) String token,
                                          @Header(AppConstants.LOCALE_CONSTANT) String locale,
                                             @Body JsonObject body);

    @Headers("Content-Type: application/json")
    @POST("v1/cart/store/add")
    Call<CartResult> getCartFromServer(@Header(AppConstants.TOKEN_CONSTANT) String token,
                                       @Header(AppConstants.LOCALE_CONSTANT) String locale,
                                       @Body JsonObject body);

    @Headers("Content-Type: application/json")
    @POST("v1/stores/quote")
    Call<QuoteResult> getQuoteFromServer(@Header(AppConstants.TOKEN_CONSTANT) String token,
                                         @Header(AppConstants.LOCALE_CONSTANT) String locale,
                                         @Body JsonObject body);

    @Headers("Content-Type: application/json")
    @GET("v1/users/cart")
    Call<UserCart> getUserCart(@Header(AppConstants.TOKEN_CONSTANT) String token,
                               @Header(AppConstants.LOCALE_CONSTANT) String locale,
                               @Query("c") String countryID);

    @Headers("Content-Type: application/json")
    @GET("v1/users/inbox")
    Call<InboxMessages> getInboxMessages(@Header(AppConstants.TOKEN_CONSTANT) String token,
                                         @Header(AppConstants.LOCALE_CONSTANT) String locale,
                                         @Query("p") String pageNumber,
                                         @Query("pl") String pageLenght);

    @Headers("Content-Type: application/json")
    @PATCH("v1/users/inbox/read")
    Call<StandarResponse> setMessageAsRead(@Header(AppConstants.TOKEN_CONSTANT) String token,
                                           @Header(AppConstants.LOCALE_CONSTANT) String locale,
                                           @Body JsonObject body);

    @Headers("Content-Type: application/json")
    @GET("v1/config")
    Call<SystemConfig> getSystemConfig(@Header(AppConstants.TOKEN_CONSTANT) String token,
                                       @Header(AppConstants.LOCALE_CONSTANT) String locale);

    @Headers("Content-Type: application/json")
    @GET("v1/users/payment_methods")
    Call<UserPayments> getUserPaymentList(@Header(AppConstants.TOKEN_CONSTANT) String token,
                                          @Header(AppConstants.LOCALE_CONSTANT) String locale,
                                          @Query("c") String countryID);

    @Headers("Content-Type: application/json")
    @PATCH("v1/users/payment_methods/{paymentID}")
    Call<StandarResponse> setCurrentPaymentMethod(@Header(AppConstants.TOKEN_CONSTANT) String token,
                                          @Header(AppConstants.LOCALE_CONSTANT) String locale,
                                               @Path("paymentID") String paymentID);

    @Headers("Content-Type: application/json")
    @DELETE("v1/users/payment_methods/{paymentID}")
    Call<StandarResponse> removeSelectedPaymentID(@Header(AppConstants.TOKEN_CONSTANT) String token,
                                               @Header(AppConstants.LOCALE_CONSTANT) String locale,
                                               @Path("paymentID") String paymentID);

    @Headers("Content-Type: application/json")
    @GET("v1/users/payment_methods/add")
    Call<NewPaymentMethod> addNewPaymentMethod(@Header(AppConstants.TOKEN_CONSTANT) String token,
                                               @Header(AppConstants.LOCALE_CONSTANT) String locale,
                                               @Query("c") String countryID);

    @Headers("Content-Type: application/json")
    @GET("v1/seller/conditions")
    Call<SellerConditions> sellerConditions(@Header(AppConstants.TOKEN_CONSTANT) String token,
                                            @Header(AppConstants.LOCALE_CONSTANT) String locale,
                                            @Query("c") String countryID);

    @Headers("Content-Type: application/json")
    @POST("v1/seller/quote")
    Call<SellerQuote> sellerQuote(@Header(AppConstants.TOKEN_CONSTANT) String token,
                                  @Header(AppConstants.LOCALE_CONSTANT) String locale,
                                  @Query("c") String countryID,
                                  @Body JsonObject body);

    @Headers("Content-Type: application/json")
    @POST("v1/seller/order")
    Call<SellerSuccess> sellerOrder(@Header(AppConstants.TOKEN_CONSTANT) String token,
                                          @Header(AppConstants.LOCALE_CONSTANT) String locale,
                                          @Query("c") String countryID,
                                          @Body JsonObject body);

    @Headers("Content-Type: application/json")
    @POST("v1/seller/showcase")
    Call<SellerShowcase> sellerShowcase(@Header(AppConstants.TOKEN_CONSTANT) String token,
                                        @Header(AppConstants.LOCALE_CONSTANT) String locale,
                                        @Query("c") String countryID,
                                        @Query("p") String pageNumber,
                                        @Query("pl") String pageLength,
                                        @Body JsonObject body);
    @Headers("Content-Type: application/json")
    @POST("v1/seller/orders")
    Call<SellerOrder> sellerOrders(@Header(AppConstants.TOKEN_CONSTANT) String token,
                                   @Header(AppConstants.LOCALE_CONSTANT) String locale,
                                   @Query("c") String countryID,
                                   @Query("p") String pageNumber,
                                   @Query("pl") String pageLength,
                                   @Body JsonObject body);

    @Headers("Content-Type: application/json")
    @POST("v1/seller/payments/detail")
    Call<PaymentDetail> getPaymentDetail(@Header(AppConstants.TOKEN_CONSTANT) String token,
                                         @Header(AppConstants.LOCALE_CONSTANT) String locale,
                                         @Query("c") String countryID,
                                         @Body JsonObject body);

    @Headers("Content-Type: application/json")
    @POST("v1/seller/payments/current")
    Call<CurrentPayment> getCurrentPayment(@Header(AppConstants.TOKEN_CONSTANT) String token,
                                           @Header(AppConstants.LOCALE_CONSTANT) String locale,
                                           @Query("c") String countryID,
                                           @Query("p") String pageNumber,
                                           @Query("pl") String pageLength,
                                           @Body JsonObject body);

    @Headers("Content-Type: application/json")
    @POST("v1/seller/payments")
    Call<PaymentHistory> getPaymentHistory(@Header(AppConstants.TOKEN_CONSTANT) String token,
                                           @Header(AppConstants.LOCALE_CONSTANT) String locale,
                                           @Query("c") String countryID,
                                           @Query("p") String pageNumber,
                                           @Query("pl") String pageLength,
                                           @Body JsonObject body);

    @Headers("Content-Type: application/json")
    @POST("v1/ebooks")
    Call<Ebook> getEbooks(@Header(AppConstants.TOKEN_CONSTANT) String token,
                          @Header(AppConstants.LOCALE_CONSTANT) String locale,
                          @Body JsonObject body,
                          @Query("p") String pageNumber,
                          @Query("pl") String pageLenght);

    @Headers("Content-Type: application/json")
    @GET("v1/seller/info")
    Call<SellerInfo> getSellerInfo(@Header(AppConstants.TOKEN_CONSTANT) String token,
                                   @Header(AppConstants.LOCALE_CONSTANT) String locale,
                                   @Query("c") String countryID);

    @Headers("Content-Type: application/json")
    @POST("v1/seller/orders/detail")
    Call<OrderDetail> getOrderDetail(@Header(AppConstants.TOKEN_CONSTANT) String token,
                                     @Header(AppConstants.LOCALE_CONSTANT) String locale,
                                     @Query("c") String countryID,
                                     @Body JsonObject body);



}
