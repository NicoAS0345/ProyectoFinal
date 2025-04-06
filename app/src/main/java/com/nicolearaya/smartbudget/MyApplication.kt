package com.nicolearaya.smartbudget

import android.app.Application
import android.os.Bundle
import android.util.Log
import com.google.firebase.Firebase
import com.google.firebase.app
import com.google.firebase.firestore.firestore
import com.google.firebase.initialize
import com.google.firebase.options
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class MyApplication : Application()


