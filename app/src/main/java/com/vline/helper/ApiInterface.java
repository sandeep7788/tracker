package com.vline.helper;

import com.vline.helper.ApiContants;
import com.google.gson.JsonObject;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.Field;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.Query;

public interface ApiInterface {

//    http://cbisolar.vidhyalaya.co.in/Api/Api/customerlogin?mobno=9828775444&password=1234&remember_token=1234
//    http://cbisolar.vidhyalaya.co.in/Api/Api/customerlogin?mobno="9828775444"&password="1234"&remember_token="1234"

    @POST(ApiContants.PREF_customer_login)
    Call<JsonObject> signIn(
            @Query("name") String mobno,
            @Query("a_number") String password,
            @Query("mobile") String mobile,
            @Query("bo_number") String bo_number);

    @POST(ApiContants.PREF_login)
    Call<JsonObject> loginOtp(@Query("mobile") Integer mobno,
                                      @Query("password") String password);

    @POST(ApiContants.PREF_updateNkk)
    Call<JsonObject> updateNkk(@Body String request);

    @POST(ApiContants.PREF_AddNkk)
    Call<JsonObject> addNkk(@Body String request);

    @POST(ApiContants.PREF_attandance)
    Call<JsonObject> attandance(@Body String request);



    @GET(ApiContants.PREF_dashboard)
    Call<JsonObject> dashboardShakaList(@Query("id") Integer id);
//
    @GET(ApiContants.PREF_dashboard)
    Call<JsonObject> dashboardShakaList(@Query("id") Integer id, @Query("user_id") Integer user_id, @Query("flag") String flag
            , @Query("flag_id") Integer flag_id);

    @GET(ApiContants.PREF_getExcel)
    Call<ResponseBody> getExcel(@Query("shakha_id") Integer shakha_id);


//    @FormUrlEncoded
//    @Headers("Content-Type: application/json")
//    @POST(ApiContants.PREF_regis)
//    Call<JsonObject> formRegistration(
//            @Field("name") String name,
//            @Field("lastname") String lastname,
//            @Field("occupation") String occupation,
//            @Field("address") String address,
//            @Field("mobile") String mobile,
//            @Field("whatsapp") String whatsapp,
//            @Field("dob") String dob,
//            @Field("blood_group") String blood_group,
//            @Field("shikshan") String shikshan,
//            @Field("shikshan_year") String shikshan_year,
//            @Field("pre_respon") String pre_respon,
//            @Field("oauth") Integer oauth,
//            @Field("ghosh") Integer ghosh,
//            @Field("vidhik_present") String vidhik_present,
//            @Field("vidhik_past") String vidhik_past,
//            @Field("createdy") Integer createdy,
//            @Field("updateddy") Integer updateddy,
//            @Field("shakha") Integer shakha,
//            @Field("other") String other,
//            @Field("flag") String flag
//            );

    @FormUrlEncoded
    @POST(ApiContants.PREF_attandance)
    Call<JsonObject> attandance(
            @Field("date") String date,
            @Field("shakha") String shakha,
            @Field("pravas") String pravas,
            @Field("pravashi_name") String pravashi_name,
            @Field("sankhya") String sankhya,
            @Field("flag") String flag
            );

    @FormUrlEncoded
    @POST(ApiContants.PREF_profileupdate)
    Call<JsonObject> profileupdate(
            @Field("customer_id") String customer_id,
            @Field("address") String address);

    @FormUrlEncoded
    @POST(ApiContants.PREF_passwordchange)
    Call<JsonObject> passwordchange(
            @Field("customer_id") String customer_id,
            @Field("old_password") String old_password,
            @Field("new_password") String new_password);

    @GET(ApiContants.PREF_StoreList)
    Call<JsonObject> listStore();

    @GET(ApiContants.PREF_winner_list)
    Call<JsonObject> winner_list();

    @GET(ApiContants.PREF_notification)
    Call<JsonObject> notification();

    @GET(ApiContants.PREF_product_list)
    Call<JsonObject> product_list();

    @GET(ApiContants.PREF_banner_list)
    Call<JsonObject> banner_list();

    @FormUrlEncoded
    @POST(ApiContants.PREF_UserAPI)
    Call<JsonObject> agent_otp_login(@Field("action") String action,
                                     @Field("cell") String cell);

    @FormUrlEncoded
    @POST(ApiContants.PREF_ClinicAPI)
    Call<JsonObject> list_doc_clinic(@Field("action") String action,
                                     @Field("clinicid") String cell,
                                     @Field("ref_code") String ref_code);

    @FormUrlEncoded
    @POST(ApiContants.PREF_UserAPI)
    Call<JsonObject> otp_login(@Field("action") String action,
                               @Field("cell") String cell);

    @FormUrlEncoded
    @POST(ApiContants.PREF_ClinicAPI)
    Call<JsonObject> list_clinic(@Field("action") String action,
                               @Field("ref_code") String ref_code,
                               @Field("ref_id") String ref_id
    );

    @FormUrlEncoded
    @POST(ApiContants.PREF_ClinicAPI)
    Call<JsonObject> get_clinic(@Field("action") String action,
                               @Field("clinicid") String ref_code
    );

    @FormUrlEncoded
    @POST(ApiContants.PREF_new_prospect)
    Call<JsonObject> new_prospect(
            @Field("action") String action,
            @Field("ref_code") String ref_code,
            @Field("providername") String providername,
            @Field("email") String email,
            @Field("dob") String dob,
            @Field("gender") String gender,
            @Field("cell") String cell);

    @FormUrlEncoded
    @POST(ApiContants.PREF_ClinicAPI)
    Call<JsonObject>    new_clinic(
            @Field("action") String action,
            @Field("ref_code") String ref_code,
            @Field("ref_id") String ref_id,
            @Field("name") String name,
            @Field("address1") String address1,
            @Field("address2") String address2,
            @Field("address3") String address3,
            @Field("city") String city,
            @Field("st") String st,
            @Field("pin") String pin,
            @Field("cell") String cell,
            @Field("telephone") String telephone,
            @Field("email") String email,
            @Field("status") String status,
            @Field("website") String website,
            @Field("gps_location") String gps_location,
            @Field("whatsapp") String whatsapp,
            @Field("facebook") String facebook,
            @Field("twitter") String twitter,
            @Field("primary") String primary,
            @Field("dentalchairs") String dentalchairs,
            @Field("auto_clave") String auto_clave,
            @Field("implantology") String implantology,
            @Field("instrument_sterilization") String instrument_sterilization,
            @Field("waste_displosal") String waste_displosal,
            @Field("suction_machine") String suction_machine,
            @Field("laser") String laser,
            @Field("RVG_OPG") String RVG_OPG,
            @Field("radiation_protection") String radiation_protection,
            @Field("computers") String computers,
            @Field("network") String network,
            @Field("internet") String internet,
            @Field("air_conditioned") String air_conditioned,
            @Field("waiting_area") String waiting_area,
            @Field("backup_power") String backup_power,
            @Field("toilet") String toilet,
            @Field("water_filter") String water_filter,
            @Field("parking_facility") String parking_facility,
            @Field("receptionist") String receptionist,
            @Field("credit_card") String credit_card,
            @Field("certifcates") String certifcates,
            @Field("emergency_drugs") String emergency_drugs,
            @Field("infection_control") String infection_control,
            @Field("daily_autoclaved") String daily_autoclaved,
            @Field("patient_records") String patient_records,
            @Field("patient_consent") String patient_consent,
            @Field("patient_traffic") String patient_traffic,
            @Field("nabh_iso_certifcation") String nabh_iso_certifcation,
            @Field("mdp_registration") String mdp_registration,
            @Field("intra_oral_camera") String intra_oral_camera,
            @Field("rotary_endodontics") String rotary_endodontics
    );

    @FormUrlEncoded
    @POST(ApiContants.PREF_appointmentAPI)
    Call<JsonObject> new_appointment(
            @Field("action") String action,
            @Field("providerid") String providerid,
            @Field("doctorid") String doctorid,
            @Field("clinicid") String clinicid,
            @Field("memberid") String memberid,
            @Field("patientid") String patientid,
            @Field("cell") String cell,
            @Field("complaint") String complaint,
            @Field("appointment_start") String appointment_start,
            @Field("duration") String duration,
            @Field("notes") String notes
    );

    @FormUrlEncoded
    @POST(ApiContants.PREF_ClinicAPI)
    Call<JsonObject> delete_clinic(
            @Field("action") String action,
            @Field("clinicid") String clinicid
    );

/*
            @Part("auto_clave") RequestBody auto_clave,
            @Part("implantology") RequestBody implantology,
            @Part("instrument_sterilization") RequestBody instrument_sterilization,
            @Part("waste_displosal") RequestBody waste_displosal,
            @Part("suction_machine") RequestBody suction_machine,
            @Part("laser") RequestBody laser,
            @Part("RVG_OPG") RequestBody RVG_OPG,
            @Part("radiation_protection") RequestBody radiation_protection,
            @Part("computers") RequestBody computers,
            @Part("network") RequestBody network,
            @Part("internet") RequestBody internet,
            @Part("air_conditioned") RequestBody air_conditioned,
            @Part("waiting_area") RequestBody waiting_area,
            @Part("backup_power") RequestBody backup_power,
            @Part("toilet") RequestBody toilet,
            @Part("water_filter") RequestBody water_filter,
            @Part("parking_facility") RequestBody parking_facility,
            @Part("receptionist") RequestBody receptionist,
            @Part("credit_card") RequestBody credit_card,
            @Part("certifcates") RequestBody certifcates,
            @Part("emergency_drugs") RequestBody emergency_drugs,
            @Part("infection_control") RequestBody infection_control,
            @Part("daily_autoclaved") RequestBody daily_autoclaved,
            @Part("patient_records") RequestBody patient_records,
            @Part("patient_consent") RequestBody patient_consent,
            @Part("patient_traffic") RequestBody patient_traffic,
            @Part("nabh_iso_certifcation") RequestBody nabh_iso_certifcation,
            @Part("mdp_registration") RequestBody mdp_registration,
            @Part("intra_oral_camera") RequestBody intra_oral_camera,
            @Part("rotary_endodontics") RequestBody rotary_endodontics
   /*         @Part MultipartBody.Part file);
*/

}