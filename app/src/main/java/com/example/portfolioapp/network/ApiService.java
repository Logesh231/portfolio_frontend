package com.example.portfolioapp.network;
import com.example.portfolioapp.model.LoginRequest;
import com.example.portfolioapp.model.LoginResponse;
import com.example.portfolioapp.model.Project;
import com.example.portfolioapp.model.Resume;
import com.example.portfolioapp.model.Skill;
import java.util.List;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.DELETE;
import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.Multipart;
import retrofit2.http.POST;
import retrofit2.http.PUT;
import retrofit2.http.Part;
import retrofit2.http.Path;

public interface ApiService {

    // ── AUTH ─────────────────────────────────────────────────
//    @POST("auth/login")
//    Call<LoginResponse> login(@Body LoginRequest request);

    @POST("api/auth/verify-otp")
    Call<LoginResponse> verifyOtp(@Body RequestBody body);

    @POST("api/auth/login")
    Call<LoginResponse> login(@Body LoginRequest request);

    // ── PROJECTS (public) ────────────────────────────────────
    @GET("api/projects")
    Call<List<Project>> getProjects();

    // ── PROJECTS (admin) — Multipart for image upload ────────
    @Multipart
    @POST("api/projects")
    Call<Project> addProject(
            @Header("Authorization") String token,
            @Part("title")       RequestBody title,
            @Part("description") RequestBody description,
            @Part("techStack")   RequestBody techStack,
            @Part("githubUrl")   RequestBody githubUrl,
            @Part MultipartBody.Part image   // nullable — image is optional
    );

    @Multipart
    @PUT("api/projects/{id}")
    Call<Project> updateProject(
            @Header("Authorization") String token,
            @Path("id") String id,
            @Part("title")       RequestBody title,
            @Part("description") RequestBody description,
            @Part("techStack")   RequestBody techStack,
            @Part("githubUrl")   RequestBody githubUrl,
            @Part MultipartBody.Part image   // nullable — only if user picked new image
    );

    @DELETE("api/projects/{id}")
    Call<Void> deleteProject(
            @Header("Authorization") String token,
            @Path("id") String id
    );

    // ── SKILLS (public) ──────────────────────────────────────
    @GET("api/skills")
    Call<List<Skill>> getSkills();

    // ── SKILLS (admin) ───────────────────────────────────────
    @POST("api/skills")
    Call<Skill> addSkill(
            @Header("Authorization") String token,
            @Body Skill skill
    );

    @PUT("api/skills/{id}")
    Call<Skill> updateSkill(
            @Header("Authorization") String token,
            @Path("id") String id,
            @Body Skill skill
    );


    @DELETE("api/skills/{id}")
    Call<Void> deleteSkill(
            @Header("Authorization") String token,
            @Path("id") String id
    );

    // ── RESUME (public) ──────────────────────────────────────
    @GET("api/resume")
    Call<Resume> getResume();

    // ── RESUME (admin) — Multipart for PDF upload ────────────
    @Multipart
    @POST("api/resume")
    Call<Resume> uploadResume(
            @Header("Authorization") String token,
            @Part MultipartBody.Part resumePdf
    );
}







