package com.investmango.hrconsole.api;

import com.investmango.hrconsole.manager.activity.ProjectsFragment;
import com.investmango.hrconsole.manager.activity.StoryRequest;
import com.investmango.hrconsole.model.AddEvent;
import com.investmango.hrconsole.model.AddSubTask;
import com.investmango.hrconsole.model.AddTask;
import com.investmango.hrconsole.model.AdminSalaryDetails;
import com.investmango.hrconsole.model.AdminTask;
import com.investmango.hrconsole.model.AllActiveUsers;
import com.investmango.hrconsole.model.AllFeedResponse;
import com.investmango.hrconsole.model.AllLeaveResponse;
import com.investmango.hrconsole.model.AllSalaryDetail;
import com.investmango.hrconsole.model.ApprovedLeaves;
import com.investmango.hrconsole.model.AssignMeeting;
import com.investmango.hrconsole.model.AssignTask;
import com.investmango.hrconsole.model.Assignment;
import com.investmango.hrconsole.model.AssignmentItem;
import com.investmango.hrconsole.model.AssignmentsResponse;
import com.investmango.hrconsole.model.Attendance;
import com.investmango.hrconsole.model.AttendanceResponse;
import com.investmango.hrconsole.model.Departments;
import com.investmango.hrconsole.model.DocsModel;
import com.investmango.hrconsole.model.DocumentModel;
import com.investmango.hrconsole.model.DocumentResponse;
import com.investmango.hrconsole.model.EmpPerformance;
import com.investmango.hrconsole.model.Event;
import com.investmango.hrconsole.model.FeedbackRequest;
import com.investmango.hrconsole.model.FeedbackResponseItem;
import com.investmango.hrconsole.model.LeaveReqResponse;
import com.investmango.hrconsole.model.LeaveRequest;
import com.investmango.hrconsole.model.LeaveRequestUpdateStatus;
import com.investmango.hrconsole.model.MeetingDetails;
import com.investmango.hrconsole.model.MeetingDetailsAdmin;
import com.investmango.hrconsole.model.MeetingListResponse;
import com.investmango.hrconsole.model.Message;
import com.investmango.hrconsole.model.MessageResponse;
import com.investmango.hrconsole.model.MonthlyPerformanceResp;
import com.investmango.hrconsole.model.PresentEmpRes;
import com.investmango.hrconsole.model.PresentEmployee;
import com.investmango.hrconsole.model.PreviousTask;
import com.investmango.hrconsole.model.Salary;
import com.investmango.hrconsole.model.SaveUserLeave;
import com.investmango.hrconsole.model.SignUp;
import com.investmango.hrconsole.model.StagesResponse;
import com.investmango.hrconsole.model.StoryResponse;
import com.investmango.hrconsole.model.SubTaskResponse;
import com.investmango.hrconsole.model.Task;
import com.investmango.hrconsole.model.TaskResponse;
import com.investmango.hrconsole.model.TodayAttendnce;
import com.investmango.hrconsole.model.TotalEmpResponseItem;
import com.investmango.hrconsole.model.UpdateMeeting;
import com.investmango.hrconsole.model.UpdateTaskStatus;
import com.investmango.hrconsole.model.User;
import com.investmango.hrconsole.model.messageItem;

import java.util.List;

import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.DELETE;
import retrofit2.http.GET;
import retrofit2.http.Multipart;
import retrofit2.http.PATCH;
import retrofit2.http.POST;
import retrofit2.http.PUT;
import retrofit2.http.Part;
import retrofit2.http.Path;
import retrofit2.http.Query;

public interface ApiInterface {
    // login endpoint
    @POST("generate-token")
    Call<User> logInUser(@Body RequestBody requestBody);

    // Refresh Token
    @POST("user/refresh-token")
    Call<User> refreshToken(@Body RequestBody requestBody);

    // Current User
//    @GET("current-user")
//    Call<User> getCurrentUser(@Header("Authorization") String token);
  // Current User
    @GET("current-user")
    Call<User> getCurrentUser();

    @GET("user/get/user/by/{user_id}")
    Call<User> getChildUser(@Path("user_id") long user_id);

    // Sign Up
    @POST("user/save/new")
    Call<SignUp> signUp( @Body SignUp requestBody);

    // Save FCM Token
    @POST("save/device/token/by/id/{user_id}")
    Call<String> saveDeviceToken(@Body RequestBody deviceToken, @Path("user_id") Long id);

//    @GET("get/meeting/by/meeting/id/{id}")
//    Call<MeetingResponse> getmeetings( @Path("id") Long id);
//
    @GET("get/leaves/of/user/by/userId/{user_id}")
    Call<AllLeaveResponse>
    getAllLeaves(@Path("user_id") Long user_id, @Query("page") int page, @Query("size") int size);

    @GET("get/leaves/of/user/by/userId/{user_id}")
    Call<AllLeaveResponse>
    getFilteredLeave(@Path("user_id") Long user_id, @Query("startDate") Long startDate, @Query("endDate") Long endDate, @Query("status") String status, @Query("page") int page);

    @GET("get/leaves/of/user/by/userId/{user_id}")
    Call<AllLeaveResponse>
    getFilteredLeaveWithoutStatus(@Path("user_id") Long user_id, @Query("startDate") Long startDate, @Query("endDate") Long endDate, @Query("page") int page);

    @GET("get/leaves/of/user/by/userId/{user_id}")
    Call<AllLeaveResponse>
    getFilteredLeaveWithOutStartDate(@Path("user_id") Long user_id, @Query("status") String status, @Query("endDate") Long endDate, @Query("page") int page);

    @GET("get/leaves/of/user/by/userId/{user_id}")
    Call<AllLeaveResponse>
    getFilteredLeaveWithOutEndDate(@Path("user_id") Long user_id, @Query("status") String status, @Query("startDate") Long startDate, @Query("page") int page);

    @GET("get/leaves/of/user/by/userId/{user_id}")
    Call<AllLeaveResponse>
    getFilteredLeaveWithStartDate(@Path("user_id") Long user_id, @Query("startDate") Long startDate, @Query("page") int page);

    @GET("get/leaves/of/user/by/userId/{user_id}")
    Call<AllLeaveResponse>
    getFilteredLeaveWithEndDate(@Path("user_id") Long user_id, @Query("endDate") Long endDate, @Query("page") int page);

    @GET("get/leaves/of/user/by/userId/{user_id}")
    Call<AllLeaveResponse>
    getFilteredLeaveWithoutDate(@Path("user_id") Long user_id, @Query("status") String status, @Query("page") int page);
 @GET("get/leaves/of/user/by/userId/{user_id}")
    Call<AllLeaveResponse>
    getFilteredLeaveWithChild(@Path("user_id") Long user_id, @Query("page") int page);

    @GET("manager/get/all/members/list/by/id/{managerId}")
    Call<List<TotalEmpResponseItem>> getTotalEmp(@Path("managerId") Long managerId, @Query("subChild") Boolean subChild);

    @GET("user/get/all/user/list")
    Call<List<TotalEmpResponseItem>> getAllEmployee(@Query("isEnabled") Boolean isEnabled);

    @GET("user/get/all/department/list")
    Call<Departments> getDepartments();


    @GET("monthly/performance/record/by/user/id/{userId}")
    Call<MonthlyPerformanceResp> getMonthPerformance(@Path("userId") Long userId, @Query("month") String month);

    @GET("monthly/performance/record/by/user/id/{userId}")
    Call<MonthlyPerformanceResp> getMonthPerformance(@Path("userId") Long userId);

    @GET("manager/get/all/members/leaves/by/id/{managerId}")
    Call<LeaveReqResponse> getPendingLeaves( @Path("managerId") Long managerId, @Query("subChild") Boolean subChild
            , @Query("managerStatus") String status, @Query("page") int page, @Query("size") int size);

 @GET("manager/get/all/members/leaves/by/id/{managerId}")
    Call<LeaveReqResponse> getLeaves( @Path("managerId") Long managerId, @Query("subChild") Boolean subChild
            , @Query("page") int page, @Query("size") int size);

    @GET("get/all/leaves")
    Call<LeaveReqResponse> getFilteredLeave( @Query("page") int page, @Query("size") int size);

    @GET("get/all/leaves")
    Call<LeaveReqResponse> getFilteredLeavewithStartDate(@Query("userId") Long userId, @Query("startDate") Long startDate);

    @GET("get/all/leaves")
    Call<LeaveReqResponse> getFilteredLeavewithEndDate(@Query("userId") Long userId, @Query("endDate") Long endDate);

    @GET("get/all/leaves")
    Call<LeaveReqResponse> getFilteredLeavewithout_endDate(@Query("userId") Long userId, @Query("status") String status, @Query("startDate") Long startDate);

    @GET("get/all/leaves")
    Call<LeaveReqResponse> getFilteredLeavewithout_startDate(@Query("userId") Long userId, @Query("status") String status, @Query("endDate") Long endDate);

    @GET("get/all/leaves")
    Call<LeaveReqResponse> getFilteredLeavewithout_status(@Query("userId") Long userId, @Query("startDate") Long startDate, @Query("endDate") Long endDate);

    @GET("get/all/leaves")
    Call<LeaveReqResponse> getFilteredLeaveWithoutDate(@Query("userId") Long userId, @Query("status") String status, @Query("page") int page, @Query("size") int size);


    @GET("manager/get/all/members/attendance/by/id/{managerId}")
    Call<PresentEmpRes> PresentEmployee(@Path("managerId") Long managerId, @Query("subChild") Boolean subChild, @Query("size") int page);

    @GET("manager/get/all/members/tasks/by/id/{managerId}")
    Call<TaskResponse> memberTaskOFManager(@Path("managerId") Long managerId, @Query("page") int page, @Query("size") int size);

//   @GET("get/all/tasks")
//    Call<TaskResponse> AdmingetAllTask();

    @GET("get/todays/task/of/all/user")
    Call<TaskResponse> AdmingetAllTask(@Query("page") int page, @Query("size") int size);

    // Send OTP on registered email id.
    @POST("forget/send-otp")
    Call<ResponseBody> sendOtp( @Query("email") String email);

    @POST("forget/send-otp")
    Call<String> NewsendOtp(@Query("email") String email);

    // Verify - OTP
    @POST("forget/verify-otp")
    Call<ResponseBody> verifyOtp(@Query("email") String email, @Query("otp") String otp);

    // Reset Password
    @POST("forget/change-password")
    Call<ResponseBody> resetPassword(@Query("email") String email, @Query("password") String password);

    // Change Password
    @PATCH("forget/update/password/by/id/{user_id}")
    Call<ResponseBody> updatePassword(
            @Path("user_id") long userId,
            @Query("password") String password
    );

    @GET("get/assignment/by/user/id/{userId}")
    Call<AssignmentsResponse> getuserAssigments(@Path("userId") long userId);

    @GET("get/assignment/by/id/{Id}")
    Call<AssignmentItem> getAssigmentById(@Path("Id") int Id);

    @GET("get/all/assignments")
    Call<AssignmentsResponse> getAllAssigments();

    @GET("get/subtask/by/id/{subtaskId}")
    Call<StoryResponse> getSubTaskById(@Path("subtaskId") int subtaskId);

    @GET("get/all/assignment/stages")
    Call<StagesResponse> getStageById(@Query("assignmentId") int assignmentId);

    @GET("get/subtask/by/assignment/id{assignmentId}")
    Call<SubTaskResponse> getSubtask(@Path("assignmentId") int assignmentId, @Query("assignToId") Long assignToId);

    // Attendance
    @POST("save/user/attendance/{user_id}")
    Call<ResponseBody> saveAttendance(
            @Body Attendance attendance,
            @Path("user_id") Long id);

    @Multipart
    @POST("/user/s3/upload/folder/docs")
    Call<String> saveImage(
            @Part MultipartBody.Part requestBody,
            @Query("folderName") String folderName);

    @PUT("update/user/attendance/{user_id}")
    Call<String> updateAttendance(@Body Attendance attendance,
                                  @Path("user_id") Long id);

    @PUT("/update/subtask/by/subtask/id/and/assignment/id/{subtaskId}")
    Call<String> updateStory(@Body StoryRequest storyResponse,
                             @Path("subtaskId") int subtaskId, @Query("assignmentId") int assignmentId);

    @PUT("/update/subtask/by/subtask/id/and/assignment/id/{subtaskId}")
    Call<String> update_Sub_Descrip(@Body RequestBody body,
                                    @Path("subtaskId") int subtaskId, @Query("assignmentId") int assignmentId);

    @PUT("/update/assignment/by/id/{Id}")
    Call<String> updateNotes(@Body ProjectsFragment.NotesRequest notesResponse,
                             @Path("Id") int Id);

    @PUT("update/new/performance/by/user/id/{user_id}")
    Call<EmpPerformance> updatePerformance(@Body EmpPerformance performance,
                                           @Path("user_id") Long id);

    @GET("get/user/attendance/{user_id}")
    Call<List<Attendance>> getUserAttendance(
            @Path("user_id") Long id);

    @GET("get/attendance/record/by/{userId}")
    Call<AttendanceResponse> getAttendance(
            @Path("userId") Long userId, @Query("page") int page);

    @GET("get/user/attendance/{user_id}/{start_date}/{end_date}")
    Call<AttendanceResponse> getUsernewAttendancebyMonth(
            @Path("user_id") Long id, @Path("start_date") Long start_date, @Path("end_date") Long end_date, @Query("size") int size);

    @PUT("update/user/attendance/{user_id}")
    Call<ResponseBody> updateUserAttendance(
            @Body Attendance attendance,
            @Path("user_id") Long id);

    @GET("get/user/monthly/attendance/count/by/{userId}")
    Call<Integer> getUserMonthlyAttendanceCount(
            @Path("userId")
            long userId);

    @GET("get/attendance/record/of/user/by/{userId}")
    Call<ResponseBody> getMonthlyAttendance(
            @Path("userId") long userId);

    @GET("get/today/attendance/{user_id}")
    Call<TodayAttendnce> getTodayAttendance(
            @Path("user_id") long userId);

    // Meeting
    @GET("get/meeting/by/attendees/by/user/id/{user_id}")
    Call<List<MeetingDetails>> getAllTodayMeeting(
            @Path("user_id") Long id
    );

    @PATCH("/update/presence/by/user/{meetingId}")
    Call<String> AcceptMeet(
            @Path("meetingId") int meetingId,
            @Query("isUserPresent") boolean isUserPresent
    );

    @GET("get/meeting/by/attendees/by/user/id/{user_id}")
    Call<MeetingListResponse> getTodayMeeting(
            @Path("user_id") Long id
    );

    @GET("get/meeting/by/attendees/by/user/id/{user_id}")
    Call<MeetingListResponse> getFilteredOwnMeeting(
            @Path("user_id") Long id,
            @Query("status") String status,
            @Query("startDate") long startDate,
            @Query("endDate") long endDate,
            @Query("page") int page,
            @Query("size") int size
    );

    @GET("get/meeting/by/attendees/by/user/id/{user_id}")
    Call<MeetingListResponse> getFilteredwithourEnddate(
            @Path("user_id") Long id,
            @Query("status") String status,
            @Query("startDate") long startDate,
            @Query("page") int page,
            @Query("size") int size
    );

    @GET("get/meeting/by/attendees/by/user/id/{user_id}")
    Call<MeetingListResponse> getFilteredwithoutStartdate(
            @Path("user_id") Long id,
            @Query("status") String status,
            @Query("endDate") long endDate,
            @Query("page") int page,
            @Query("size") int size
    );

    @GET("get/meeting/by/attendees/by/user/id/{user_id}")
    Call<MeetingListResponse> getFilteredwithStartdate(
            @Path("user_id") Long id,
            @Query("startDate") long startDate,
            @Query("page") int page,
            @Query("size") int size
    );

    @GET("get/meeting/by/attendees/by/user/id/{user_id}")
    Call<MeetingListResponse> getFilteredwithEnddate(
            @Path("user_id") Long id,
            @Query("startDate") long startDate,
            @Query("page") int page,
            @Query("size") int size
    );

    @GET("get/meeting/by/attendees/by/user/id/{user_id}")
    Call<MeetingListResponse> getFilteredwithoutStaus(
            @Path("user_id") Long id,
            @Query("startDate") long startDate,
            @Query("endDate") long endDate,
            @Query("page") int page,
            @Query("size") int size
    );

    @GET("get/meeting/by/attendees/by/user/id/{user_id}")
    Call<MeetingListResponse> getFilteredWithstatus(
            @Path("user_id") Long id,
            @Query("status") String status,
            @Query("page") int page,
            @Query("size") int size
    );

    @POST("assign/meeting/by/user/id/{userId}")
    Call<AssignMeeting> assignMeetingUser(
            @Path("userId") long userId,
            @Body AssignMeeting meetingObj
    );

    @PUT("update/meeting/by/user/id/{user_id}")
    Call<AssignMeeting> editMeetingUser(
            @Path("user_id") long userId,
            @Body AssignMeeting meetingObj
    );

    @POST("assign/meeting/by/user/id/{userId}")
    Call<AssignMeeting> assignMeetingDepartment(
            @Path("userId") long userId,
            @Body AssignMeeting meetingObj,
            @Query("department") String department
    );

    @POST("custom-chat/save/new")
    Call<String> sendMessage(
            @Body messageItem meetingObj
    );

    @GET("get/today/all/attendance/list")
    Call<List<PresentEmployee>> getAllTodayAttendance();

    @GET("get/today/all/attendance/list")
    Call<PresentEmpRes> newgetAllTodayAttendance(
            @Query("size") int size
    );

    // Salary
    @GET("get/salary/by/user/id/{user_id}")
    Call<List<Salary>> userSalary(
            @Path("user_id") Long userId
    );

    @GET("get/all/time/total/salary/cycle/by/user/{user_id}")
    Call<AllSalaryDetail> allSalaryTaken(
            @Path("user_id") Long id
    );

    @GET("get/all/salary")
    Call<List<AdminSalaryDetails>> getAllSalaryDetails();

    // Task
    @PUT("update/task/status/by/user/id/{user_id}")
    Call<ResponseBody> updateUserTaskStatus(
            @Body UpdateTaskStatus requestBody,
            @Path("user_id") long userId
    );

    @GET("get/task/by/user/id/{user_id}")
    Call<List<Task>> getAllTask(
            @Path("user_id") Long id

    );

    @DELETE("/user/s3/delete/folder/docs")
    Call<String> deleteDocument(
            @Query("file") String file

    );
 @DELETE("/delete/story/by/{Id}")
    Call<String> deleteStory(
            @Path("Id") Integer Id

    );

    @GET("get/task/by/user/id/{user_id}")
    Call<TaskResponse> getAllTaskwithpage(
            @Path("user_id") Long id,
            @Query("page") int page,
            @Query("taskStatus") String taskStatus,
            @Query("size") int size
    );

    @GET("get/task/by/user/id/{user_id}")
    Call<TaskResponse> getFilterTaskwithpage(
            @Path("user_id") Long id,
            @Query("page") int page,
            @Query("taskStatus") String taskStatus,
            @Query("startDate") long startDate,
            @Query("endDate") long endDate,
            @Query("size") int size
    );

    @GET("get/task/by/user/id/{user_id}")
    Call<TaskResponse> getFilterTaskwithoutEndDate(
            @Path("user_id") Long id,
            @Query("page") int page,
            @Query("taskStatus") String taskStatus,
            @Query("startDate") long startDate,
            @Query("size") int size
    );

  @GET("get/task/by/user/id/{user_id}")
    Call<TaskResponse> getFilterTaskwithId(
            @Path("user_id") Long id,
            @Query("page") int page,
            @Query("size") int size
    );

    @GET("get/task/by/user/id/{user_id}")
    Call<TaskResponse> getFilterTaskwithStartDate(
            @Path("user_id") Long id,
            @Query("page") int page,
            @Query("startDate") long startDate,
            @Query("size") int size
    );

    @GET("get/task/by/user/id/{user_id}")
    Call<TaskResponse> getFilterTaskwithEndDate(
            @Path("user_id") Long id,
            @Query("page") int page,
            @Query("endDate") long endDate,
            @Query("size") int size
    );

    @GET("get/task/by/user/id/{user_id}")
    Call<TaskResponse> getFilteredLeaveWithoutStartDate(
            @Path("user_id") Long id,
            @Query("page") int page,
            @Query("taskStatus") String taskStatus,
            @Query("endDate") long endDate,
            @Query("size") int size
    );

    @GET("get/task/by/user/id/{user_id}")
    Call<TaskResponse> getFilteredLeaveWithoutStatus(
            @Path("user_id") Long id,
            @Query("page") int page,
            @Query("startDate") long startDate,
            @Query("endDate") long endDate,
            @Query("size") int size
    );

    @GET("get/task/by/user/id/{user_id}")
    Call<TaskResponse> getFilterTaskwithpageWithoutDate(
            @Path("user_id") Long id,
            @Query("page") int page,
            @Query("taskStatus") String taskStatus,
            @Query("size") int size
    );

    @GET("get/task/by/user/id/{user_id}")
    Call<List<Task>> getFilterTask(
            @Path("user_id") Long id,
            @Query("taskStatus") String taskStatus
    );


    @POST("save/tasks/by/user/id/{user_id}")
    Call<AddTask> addTask(
            @Body AddTask task, @Path("user_id") Long id
    );

    @POST("add/subtask/for/assignment/{assignmentId}")
    Call<String> addSubTask(
            @Path("assignmentId") int id,
            @Body AddSubTask task
    );

    @POST("assign/task/to/employee/by/id")
    Call<AssignTask> assignTaskUser(
            @Query("user_id") long selectedUserId,
            @Body AssignTask assignTask
    );

    @PUT("update/task/by/user/id/{user_id}")
    Call<AddTask> addComment(
            @Body AddTask task, @Path("user_id") Long id
    );

    @GET("get/todays/task/of/all/user")
    Call<List<AdminTask>> getUserAllTask();

    @GET("get/all/previous/task")
    Call<List<PreviousTask>> getPreviousTask();

    @GET("monthly-statistics/by/{user_id}")
    Call<ResponseBody> getMonthlyTaskStatistics(@Path("user_id") long userId);

    // Leave
    @POST("save/leave/by/user/id/{user_id}")
    Call<SaveUserLeave> saveUserLeave(
            @Body RequestBody requestBody,
            @Path("user_id") Long id
    );


    @GET("get/leaves/of/user/by/userId/{user_id}")
    Call<List<SaveUserLeave>> getUserLeave(
            @Path("user_id") Long id
    );

    @PATCH("update/leave/status/by/user/id/{user_id}")
    Call<Void> ApproveLeaves(
            @Body LeaveRequestUpdateStatus requestBody,
            @Path("user_id") long userId
    );

    @GET("get/all/pending/leaves")
    Call<List<LeaveRequest>> getAllPendingLeave();

    @GET("get/all/pending/leaves")
    Call<LeaveReqResponse> newgetAllPendingLeave();
    @GET("get/all/approved/leaves")
    Call<List<ApprovedLeaves>> getApprovedLeaves();

    // Meeting
    @GET("get/meeting/by/meeting/host/{user_id}")
    Call<List<MeetingDetailsAdmin>> getAllMeeting(
            @Path("user_id") Long id
    );

    @PUT("update/meeting/status/by/user/id/{user_id}")
    Call<ResponseBody> updateAdminMeetingStatus(
            @Body UpdateMeeting requestBody,
            @Path("user_id") long userId
    );

    // Assignment
    @GET("get/all/pending/assignments")
    Call<List<Assignment>> getAssignment();

    // All active user
    @GET("user/active/all")
    Call<List<AllActiveUsers>> getAllActiveUser();

    // PDF generate
    @GET("generate/active/user/pdf")
    Call<Void> downloadPdf();

    // Excel generate
    @GET("generate/active/user/excel")
    Call<Void> downloadExcel();

    @GET("generate/get/user/salary/pdf/{user_id}")
    Call<Void> getUserSalaryPdf(@Path("user_id") long userId);

    @GET("generate/get/user/task/pdf/{user_id}")
    Call<Void> getTask(@Path("user_id") long userId);

    @GET("generate/get/user/task/pdf/{user_id}")
    Call<Void> getFilteredTaskDownload(@Path("user_id") long userId, @Query("fromDate") long fromDate, @Query("toDate") long toDate);

    // Update Profile
    @PATCH("user/update/image/on/cloud/by/{user_id}")
    Call<ResponseBody> uploadFile(
            @Path("user_id") long userId,
            @Query("file") String file);


    @PATCH("user/save/user/doc/by/{user_id}")
    Call<DocumentModel> saveDocByUserId(
            @Body DocumentModel documentModel,
            @Path("user_id") Long userId
    );

    @PATCH("user/save/user/doc/by/{user_id}")
    Call<String> saveDocument(
            @Body DocumentResponse documentModel,
            @Path("user_id") Long userId
    );

    @PATCH("verify/user/doc/by/{employeeId}")
    Call<ResponseBody> verifyEmpDocument(
            @Path("employeeId") long employeeId,
            @Query("isverified") boolean isverified
    );

    @PATCH("update/user/doc/by/{user_id}")
    Call<DocumentModel> updateEmpDocument(
            @Body DocumentModel documentModel,
            @Path("user_id") Long userId
    );


    @GET("get/user/doc/by/{user_id}")
    Call<DocsModel> getDocs( @Path("user_id") long userId);

    @GET("get/user/doc/by/{user_id}")
    Call<ResponseBody> getDoc( @Path("user_id") long userId);

    @GET("get/user/doc/by/{user_id}")
    Call<DocumentResponse> getdocument(@Path("user_id") long userId);

    @GET("get/feedback/by/user/id/{userId}")
    Call<List<FeedbackResponseItem>> getfeedBack(@Path("userId") long userId);

    @GET("/get/all/feedbacks")
    Call<AllFeedResponse> getAllfeedBack();

    // Feedback
    @POST("save/new/feedback/by/user/id/{userId}")
    Call<FeedbackRequest> saveNewFeedbacks(
            @Path("userId") Long userId,
            @Body FeedbackRequest feedbackRequest
    );

    // Performance
    @GET("get/performace/of/employee/by/employee/id/{user_id}")
    Call<List<EmpPerformance>> getSingleEmployeeAllPerformanceByEmpId(@Path("user_id") long userId);

    // Event
    @POST("save/new/announcement")
    Call<AddEvent> saveNewAnnouncement( @Body AddEvent addEvent);

    @GET("get/upcoming/events")
    Call<List<Event>> upcomingEvents();

    @GET("custom-chat/get/by/user/id/{userId}")
    Call<List<Message>> getCustomMessage( @Path("userId") long userId);

    @GET("custom-chat/get/by/user/id/{userId}")
    Call<MessageResponse> getNewCustomMessage(@Path("userId") long userId, @Query("page") int page);

    @GET("custom-chat/get/by/user/id/{userId}")
    Call<MessageResponse> getFilteredMessage(@Path("userId") long userId, @Query("startDate") long startDate, @Query("endDate") long endDate);
}
