package com.example.diplomski

import android.Manifest
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.os.Build
import android.os.Bundle
import android.view.WindowManager
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Modifier
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.diplomski.screens.Login
import com.example.diplomski.ui.theme.DiplomskiTheme
import androidx.compose.runtime.getValue
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavType
import androidx.navigation.navArgument
import com.example.diplomski.database.tables.User
import com.example.diplomski.retrofit.PreferencesManager
import com.example.diplomski.retrofit.TokenManager
import com.example.diplomski.screens.ActiveSession
import com.example.diplomski.screens.Checklist
import com.example.diplomski.screens.CreateATest
import com.example.diplomski.screens.Folder
import com.example.diplomski.screens.Group
import com.example.diplomski.screens.GroupNote
import com.example.diplomski.screens.GroupView
import com.example.diplomski.screens.Notes
import com.example.diplomski.screens.Notifications
import com.example.diplomski.screens.Profile
import com.example.diplomski.screens.Quiz
import com.example.diplomski.screens.QuizComments
import com.example.diplomski.screens.Register
import com.example.diplomski.screens.Reminders
import com.example.diplomski.screens.Settings
import com.example.diplomski.screens.StartScreen
import com.example.diplomski.screens.StudySession
import com.example.diplomski.screens.Test
import com.example.diplomski.screens.ViewNote
import com.example.diplomski.viewmodels.AuthState
import com.example.diplomski.viewmodels.ChecklistViewModel
import com.example.diplomski.viewmodels.FolderViewModel
import com.example.diplomski.viewmodels.GroupViewModel
import com.example.diplomski.viewmodels.MessageViewModel
import com.example.diplomski.viewmodels.QuizViewModel
import com.example.diplomski.viewmodels.ReminderViewModel
import com.example.diplomski.viewmodels.SessionViewModel
import com.example.diplomski.viewmodels.UserViewModel
import kotlin.math.abs
import kotlin.math.sqrt
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
class MainActivity : ComponentActivity(), SensorEventListener{

    private lateinit var sensorManager: SensorManager
    private var sensorSelected: Sensor? = null
    private lateinit var sessionViewModel : SessionViewModel

    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        sessionViewModel = ViewModelProvider(this, SessionViewModel.Factory)[SessionViewModel::class.java]
        sensorManager = getSystemService(SENSOR_SERVICE) as SensorManager
        sensorSelected = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)
        TokenManager.init(this)
        PreferencesManager.init(this)
        createNotificationChannel(this)
        createNotificationChannel2(this)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            requestPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
        }
        if (CameraPermissionRequester.hasPermissions(this)) {
            showContent(sessionViewModel)
        } else {
            CameraPermissionRequester.requestPermissions(this, onCameraGranted = { showContent(sessionViewModel) })
        }

    }

    override fun onStop() {
        super.onStop()
        if (sessionViewModel.uiStateSession.isRunning) {
            sessionViewModel.cancelSession()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        if (sessionViewModel.uiStateSession.isRunning) {
            sessionViewModel.cancelSession()
        }
    }
    private fun showContent(sessionViewModel : SessionViewModel){
        enableEdgeToEdge()
        setContent {
            DiplomskiTheme {
                val isRunning = sessionViewModel.uiStateSession.isRunning

                LaunchedEffect(isRunning) {
                    if (isRunning) {
                        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
                    } else {
                        window.clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
                    }
                }
                StudyApp(sessionViewModel)
            }
        }
    }

    override fun onResume() {
        super.onResume()
        sensorSelected?.let {
            sensorManager.registerListener(
                this,
                sensorSelected,
                SensorManager.SENSOR_DELAY_NORMAL
            )
        }
    }

    override fun onPause() {
        super.onPause()
        sensorManager.unregisterListener(this)
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {
        //
    }

    override fun onSensorChanged(event: SensorEvent?) {
        if (event != null) {
            val x = event.values[0]
            val y = event.values[1]
            val z = event.values[2]

            val magnitude = sqrt((x * x + y * y + z * z).toDouble()).toFloat()
            val gravity = 9.8f

            if (abs(magnitude - gravity) > 2.0f) {
                sessionViewModel.onMovementDetected()
            }
        }
    }

}

@Composable
fun StudyApp(sessionViewModel : SessionViewModel){
    val navController = rememberNavController()

    val currentBackStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = currentBackStackEntry?.destination
    val currentRoute = currentDestination?.route ?: Register.route
    val viewModel: UserViewModel = viewModel(factory = UserViewModel.Factory)
    val quizViewModel: QuizViewModel = viewModel(factory = QuizViewModel.Factory)
    val folderViewModel : FolderViewModel = viewModel(factory = FolderViewModel.Factory)
    val checklistViewModel : ChecklistViewModel = viewModel(factory = ChecklistViewModel.Factory)
    val reminderViewModel : ReminderViewModel = viewModel(factory = ReminderViewModel.Factory)
    val messageViewModel: MessageViewModel = viewModel(factory = MessageViewModel.Factory)
    val groupViewModel: GroupViewModel = viewModel(factory = GroupViewModel.Factory)
    val loggedUser : User? by viewModel.logged.collectAsState(null)
    val showList = listOf("notes", "groups", "session", "quiz", "checklist", "reminder")
    val messages = messageViewModel.messages.collectAsState()
    val isRefreshing = viewModel.isRefreshing

    LaunchedEffect(loggedUser?.id) {
        loggedUser?.let { viewModel.syncLoggedUser(it)
            messageViewModel.getMessages(loggedUser!!)
            sessionViewModel.getSessions(loggedUser!!.id)
        }
    }

    Scaffold(
        bottomBar = {
            if(currentRoute.split('/')[0] in showList) {
                NavigationBar {
                    Destinations.forEach { navDestination ->
                        if(navDestination.route in showList && navDestination.route != "profile") {
                            NavigationBarItem(
                                icon = {
                                    Icon(
                                        imageVector = navDestination.icon,
                                        contentDescription = null,
                                    )
                                },
                                label = { Text(text = navDestination.route) },
                                selected = currentRoute.startsWith(navDestination.route),
                                onClick = {
                                    val newRoute = navDestination.route
                                    navController.navigate(newRoute) {
                                        launchSingleTop = true
                                        restoreState = true
                                    }
                                },
                            )
                        }
                    }
                }
            }
        }
    ) {
        val authState by viewModel.authState.collectAsState()
        PullToRefreshBox(
            isRefreshing = isRefreshing,
            onRefresh = {
                viewModel.refreshData()
            }
        ) {
        NavHost(
            navController = navController,
            startDestination = when (authState) {
                is AuthState.Loading -> StartPage.route
                is AuthState.LoggedIn -> Quiz.route
                is AuthState.LoggedOut -> Login.route
            },
            modifier = Modifier.padding(it),
        ) {
            composable(route = Register.route) {
                Register({navController.navigate(Login.route)}, viewModel,
                    {navController.navigate(Checklist.route)})
            }
            composable(route = StartPage.route) {
                StartScreen()
            }
            composable(
                route = "${GroupView.route}/{groupId}",
                arguments = listOf(navArgument("groupId") { type = NavType.IntType }),
            ) { navBackStackEntry ->
                val groupId = navBackStackEntry.arguments?.getInt("groupId") ?: -1
                GroupView(groupId, groupViewModel, quizViewModel, folderViewModel,loggedUser,
                    {q -> navController.navigate("${TakeTest.route}/${q}")},
                    {i -> navController.navigate("${GroupNote.route}/${i}")},
                    {i -> navController.navigate("${QuizComment.route}/${i}")},
                    {navController.navigate(Group.route)})
            }
            composable(route = Notifications.route) {
                Notifications(loggedUser, messageViewModel)
            }
            composable(route = Group.route) {
                Group(groupViewModel, loggedUser, {navController.navigate(Profile.route)},
                    {navController.navigate(Notifications.route)}, messages.value.size,
                    {q -> navController.navigate("${GroupView.route}/${q}")})
            }
            composable(route = Settings.route) {
                Settings(viewModel, loggedUser) { navController.navigate(Login.route) }
            }
            composable(route = Login.route) {
                Login({navController.navigate(Register.route)}, viewModel,
                    {navController.navigate(Quiz.route)})
            }
            composable(route = Profile.route) {
                Profile(viewModel, quizViewModel,sessionViewModel,
                    loggedUser, {navController.navigate(Login.route)},
                    {navController.navigate(Settings.route)})
            }
            composable(route = Quiz.route) {
                Quiz(quizViewModel, folderViewModel, groupViewModel, loggedUser,
                    {navController.navigate(CreateTest.route)},
                    {q -> navController.navigate("${TakeTest.route}/${q}")},
                    {navController.navigate(Profile.route)},
                    {navController.navigate(Notifications.route)}, messages.value.size)
            }
            composable(route = Checklist.route) {
                Checklist(checklistViewModel, loggedUser,
                    {navController.navigate(Profile.route)},
                    {navController.navigate(Notifications.route)}, messages.value.size)
            }
            composable(route = Reminder.route) {
                Reminders(reminderViewModel, loggedUser,
                    {navController.navigate(Profile.route)},
                    {navController.navigate(Notifications.route)}, messages.value.size)
            }
            composable(route = Notes.route) {
                Notes(folderViewModel, loggedUser,
                    {id -> navController.navigate("${Files.route}/${id}")},
                    {navController.navigate(Profile.route)},
                    {navController.navigate(Notifications.route)}, messages.value.size)
            }
            composable(route = StudySession.route) {
                StudySession(sessionViewModel, loggedUser, {navController.navigate(ActiveSession.route)},
                    {navController.navigate(Profile.route)},
                    {navController.navigate(Notifications.route)}, messages.value.size)
            }
            composable(route = ActiveSession.route) {
                ActiveSession(loggedUser?.id ?: 0, sessionViewModel
                ) { navController.navigate(StudySession.route) }
            }
            composable(route = CreateTest.route) {
                CreateATest(quizViewModel, loggedUser) { navController.navigate(Quiz.route) }
            }
            composable(route = "${Files.route}/{id}",
                arguments = listOf(navArgument("id") { type = NavType.IntType }),
            ) {navBackStackEntry ->
                val id = navBackStackEntry.arguments?.getInt("id") ?: -1
                Folder(folderViewModel, id,
                    {id -> navController.navigate("${ViewNote.route}/${id}")},
                    loggedUser, {id -> navController.navigate("${Files.route}/${id}"){
                        popUpTo("${Files.route}/$id") {
                            inclusive = true
                        }
                    }},
                    {navController.navigate(Profile.route)},
                    {navController.navigate(Notifications.route)},
                    messages.value.size, groupViewModel
                )
            }
            composable(
                route = "${TakeTest.route}/{quiz}",
                arguments = listOf(navArgument("quiz") { type = NavType.IntType }),
            ) { navBackStackEntry ->
                val quizId = navBackStackEntry.arguments?.getInt("quiz") ?: -1
                Test(loggedUser, {navController.navigate(Quiz.route)}, quizId)
            }
            composable(
                route = "${ViewNote.route}/{noteId}",
                arguments = listOf(navArgument("noteId") { type = NavType.IntType }),
            ) { navBackStackEntry ->
                val noteId = navBackStackEntry.arguments?.getInt("noteId") ?: -1
                ViewNote(noteId, folderViewModel)
            }

            composable(
                route = "${GroupNote.route}/{noteId}",
                arguments = listOf(navArgument("noteId") { type = NavType.IntType }),
            ) { navBackStackEntry ->
                val noteId = navBackStackEntry.arguments?.getInt("noteId") ?: -1
                GroupNote(noteId,loggedUser,groupViewModel
                ) { id -> navController.navigate("${GroupView.route}/${id}") }
            }
            composable(
                route = "${QuizComment.route}/{noteId}",
                arguments = listOf(navArgument("noteId") { type = NavType.IntType }),
            ) { navBackStackEntry ->
                val noteId = navBackStackEntry.arguments?.getInt("noteId") ?: -1
                QuizComments(noteId, groupViewModel, loggedUser
                )
            }
        }
            }
    }
}

