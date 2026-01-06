//package com.digitalview.sdsiptv.Player
//
//import android.view.View
//import android.widget.FrameLayout
//import android.widget.ProgressBar
//import com.digitalview.sdsiptv.Activity.LiveTvPreview
//import com.digitalview.sdsiptv.Activity.PlayerActivity
//import com.digitalview.sdsiptv.Utils.LoopForward
//import kotlinx.coroutines.CoroutineScope
//import kotlinx.coroutines.Dispatchers
//import kotlinx.coroutines.delay
//import kotlinx.coroutines.launch
//
//class WhileKtPressed {
//
//    fun startLoop(broadcastForwardRewind: BroadcastForwardRewind) {
//        CoroutineScope(Dispatchers.IO).launch {
//            while (LiveTvPreview.keyIsDown) {
//                CoroutineScope(Dispatchers.Main).launch {
//                    broadcastForwardRewind.method()
//                }
//                delay(700)
//            }
//        }
//    }
//
//    fun loopHideProgress(currentTimeCalled: Long, progressBar: ProgressBar) {
//        CoroutineScope(Dispatchers.IO).launch {
//            delay(3000)
//            if (PlayerActivity.lastCalledForwardCurrentTime == currentTimeCalled) {
//                CoroutineScope(Dispatchers.Main).launch {
//                    progressBar.visibility = View.GONE
//                    CatchupHelper.firstRewind = 3000
//                }
//            }
//        }
//    }
//
//    fun loopForward(keyCode : Int, method: LoopForward) {
//        CoroutineScope(Dispatchers.IO).launch {
//            CatchupHelper.isInLoop = true;
//            while (!CatchupHelper.btnOkPressed && keyCode == CatchupHelper.actualKeyCode) {
//                CoroutineScope(Dispatchers.Main).launch {
//                    method.method()
//                }
//                delay(500)
//            }
//            CatchupHelper.isInLoop = false;
//            CatchupHelper.secsForward = 30 * 1000;
//            CatchupHelper.velocity = 2;
//        }
//    }
//
//
//
//    fun loopHideProgress(
//        currentTimeCalled: Long,
//        containerForward : FrameLayout,
//        containerRewind : FrameLayout
//    ) {
//        CoroutineScope(Dispatchers.IO).launch {
//            delay(1000)
//            if (PlayerActivity.lastCalledForwardCurrentTime == currentTimeCalled) {
//                CoroutineScope(Dispatchers.Main).launch {
//                    PlayerActivity.controllerVisibleByForward = false;
//                    CatchupHelper.firstRewind = 3000
//                    containerForward.visibility = View.GONE;
//                    containerRewind.visibility = View.GONE;
//                    CatchupHelper.counter = 2;
//                }
//            }
//        }
//    }
//
//}