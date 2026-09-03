package com.example.diplomski.retrofit

import com.example.diplomski.retrofit.dto.AuthRequest
import com.example.diplomski.retrofit.dto.AuthResponse
import com.example.diplomski.retrofit.dto.ChecklistDto
import com.example.diplomski.retrofit.dto.CommentDto
import com.example.diplomski.retrofit.dto.FolderDto
import com.example.diplomski.retrofit.dto.GroupDto
import com.example.diplomski.retrofit.dto.GroupMemberDto
import com.example.diplomski.retrofit.dto.GroupNoteDto
import com.example.diplomski.retrofit.dto.GroupQuizDto
import com.example.diplomski.retrofit.dto.MessageDto
import com.example.diplomski.retrofit.dto.NoteDto
import com.example.diplomski.retrofit.dto.QuestionDto
import com.example.diplomski.retrofit.dto.QuizDto
import com.example.diplomski.retrofit.dto.ScoreDto
import com.example.diplomski.retrofit.dto.SessionDto
import com.example.diplomski.retrofit.dto.UserDto
import okhttp3.MultipartBody
import okhttp3.ResponseBody
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Part
import retrofit2.http.Path
import retrofit2.http.Streaming

interface BackendApi {

    @POST("/auth/login")
    suspend fun login(@Body request: AuthRequest): AuthResponse

    @POST("/auth/register")
    suspend fun register(@Body request: AuthRequest): AuthResponse

    @GET("/quiz/{userId}")
    suspend fun getQuizzes(@Path("userId") userId: Long): List<QuizDto>

    @GET("/quiz/id/{quizId}")
    suspend fun getQuizById(@Path("quizId") quizId: Long): QuizDto

    @POST("/quiz")
    suspend fun createQuiz(@Body quiz: QuizDto): QuizDto

    @PUT("/quiz/update/{id}")
    suspend fun updateQuiz(@Path("id") id: Long, @Body item: QuizDto): QuizDto

    @GET("/quiz/questions/{quizId}")
    suspend fun getQuestions(@Path("quizId") quizId: Long): List<QuestionDto>

    @POST("/quiz/questions")
    suspend fun createQuestion(@Body question: QuestionDto): QuestionDto

    @DELETE("/quiz/{id}")
    suspend fun deleteQuiz(@Path("id") id: Long)

    @GET("/session/{userId}")
    suspend fun getSessions(@Path("userId") userId: Long): List<SessionDto>

    @POST("/session")
    suspend fun createSession(@Body session: SessionDto): SessionDto

    @DELETE("/session/{id}")
    suspend fun deleteSession(@Path("id") id: Long)

    @GET("/score/{userId}")
    suspend fun getScores(@Path("userId") userId: Long): List<ScoreDto>

    @POST("/score")
    suspend fun createScore(@Body score: ScoreDto): ScoreDto

    @GET("/folder/{userId}")
    suspend fun getFolders(@Path("userId") userId: Long): List<FolderDto>

    @POST("/folder")
    suspend fun createFolder(@Body folder: FolderDto): FolderDto

    @DELETE("/folder/{id}")
    suspend fun deleteFolder(@Path("id") id: Long)

    @PUT("/folder/update/{id}")
    suspend fun updateFolder(@Path("id") id: Long, @Body item: FolderDto): FolderDto

    @GET("/note/{folderId}")
    suspend fun getNotes(@Path("folderId") folderId: Long): List<NoteDto>

    @POST("/note")
    suspend fun createNote(@Body note: NoteDto): NoteDto

    @DELETE("/note/{id}")
    suspend fun deleteNote(@Path("id") id: Long)

    @PUT("/note/update/{id}")
    suspend fun updateNote(@Path("id") id: Long, @Body item: NoteDto): NoteDto

    @GET("/checklist/{userId}")
    suspend fun getChecklist(@Path("userId") userId: Long): List<ChecklistDto>

    @POST("/checklist")
    suspend fun createChecklist(@Body item: ChecklistDto): ChecklistDto

    @DELETE("/checklist/{id}")
    suspend fun deleteChecklist(@Path("id") id: Long)

    @Multipart
    @POST("/files/upload")
    suspend fun uploadFile(@Part file: MultipartBody.Part): Map<String, String>

    @GET("/files/download/{fileName}")
    @Streaming
    suspend fun downloadFile(@Path("fileName") fileName: String): ResponseBody

    @POST("/message")
    suspend fun createMessage(@Body message: MessageDto): MessageDto

    @DELETE("/message/{id}")
    suspend fun deleteMessage(@Path("id") id: Long)

    @GET("/message/{userId}")
    suspend fun getMessages(@Path("userId") userId: Long): List<MessageDto>

    @GET("/message/{userId}/{groupId}")
    suspend fun getMessage(@Path("userId") userId: Long,
                            @Path("groupId") groupId: Long): retrofit2.Response<MessageDto>

    @GET("/user/search/{username}")
    suspend fun searchUser(@Path("username") username: String): UserDto

    @PUT("/user/update/{id}")
    suspend fun updateUser(@Path("id") id: Long, @Body item: UserDto): UserDto

    @DELETE("/user/{id}")
    suspend fun deleteUser(@Path("id") id: Long)

    @POST("/group")
    suspend fun createGroup(@Body message: GroupDto): GroupDto

    @DELETE("/group/{id}")
    suspend fun deleteGroup(@Path("id") id: Long)

    @GET("/group/id/{groupId}")
    suspend fun getGroupById(@Path("groupId") groupId: Long): GroupDto

    @PUT("/group/update/{id}")
    suspend fun updateGroup(@Path("id") id: Long, @Body item: GroupDto): GroupDto

    @POST("/groupmember")
    suspend fun createGroupMember(@Body message: GroupMemberDto): GroupMemberDto

    @DELETE("/groupmember/{id}")
    suspend fun deleteGroupMember(@Path("id") id: Long)

    @DELETE("/groupmember/delete/{id}")
    suspend fun deleteAllGroupMembers(@Path("id") id: Long)

    @GET("/groupmember/{groupId}")
    suspend fun getGroupMembers(@Path("groupId") groupId: Long): List<GroupMemberDto>

    @POST("/groupnote")
    suspend fun createGroupNote(@Body message: GroupNoteDto): GroupNoteDto

    @DELETE("/groupnote/{id}")
    suspend fun deleteGroupNote(@Path("id") id: Long)

    @GET("/groupnote/{groupId}")
    suspend fun getGroupNotes(@Path("groupId") groupId: Long): List<GroupNoteDto>

    @POST("/groupquiz")
    suspend fun createGroupQuiz(@Body message: GroupQuizDto): GroupQuizDto

    @DELETE("/groupquiz/{id}")
    suspend fun deleteGroupQuiz(@Path("id") id: Long)

    @GET("/groupquiz/{groupId}")
    suspend fun getGroupQuizzes(@Path("groupId") groupId: Long): List<GroupQuizDto>

    @GET("/groupmember/user/{userId}")
    suspend fun  getUserGroups(@Path("userId") userId: Long): List<GroupMemberDto>


    @GET("/user/{id}")
    suspend fun getUserById(@Path("id") id: Long): UserDto

    @GET("/note/id/{id}")
    suspend fun getNoteById(@Path("id") id: Long): NoteDto

    @POST("/comment")
    suspend fun createComment(@Body message: CommentDto): CommentDto

    @GET("/comment/note/{noteId}")
    suspend fun  getNoteComments(@Path("noteId") noteId: Long): List<CommentDto>

    @GET("/comment/quiz/{quizId}")
    suspend fun  getQuizComments(@Path("quizId") quizId: Long): List<CommentDto>

    @DELETE("/comment/delete/{id}")
    suspend fun deleteComment(@Path("id") id: Long)

}