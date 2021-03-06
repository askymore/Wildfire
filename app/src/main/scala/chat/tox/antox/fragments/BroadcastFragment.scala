package chat.tox.antox.fragments

import java.text.Collator
import java.util

import android.app.Fragment
import android.content.{Context, Intent}
import android.content.res.ColorStateList
import android.os.Bundle
import android.support.design.widget.FloatingActionButton
import android.text.InputFilter.LengthFilter
import android.text.{InputFilter, Editable, TextWatcher}
import android.view.View.OnClickListener
import android.view.{LayoutInflater, View, ViewGroup}
import android.widget.AdapterView.OnItemClickListener
import android.widget._
import chat.tox.antox.adapters.MsgListAdapter
import chat.tox.antox.data.State
import chat.tox.antox.{utils, R}
import chat.tox.antox.tox.{MessageHelper, ToxSingleton}
import chat.tox.antox.utils.{MsgItem, TimestampUtils, AntoxLog, Constants}
import chat.tox.antox.wrapper.Message
import im.tox.tox4j.core.enums.ToxMessageType
import rx.lang.scala.schedulers.{IOScheduler, AndroidMainThreadScheduler}
import rx.lang.scala.{Observable, Subscription}
import java.sql.Timestamp
import scala.collection.mutable.ArrayBuffer

/**
 * Created by Ann on 1/13/16.
 */
/*class BroadcastFragment extends android.support.v4.app.Fragment with View.OnClickListener{
  override def onCreateView(inflater: LayoutInflater, container: ViewGroup, savedInstanceState: Bundle): View = {
    val rootView = inflater.inflate(R.layout.fragment_broadcast, container, false)
    val button = rootView.findViewById(R.id.broadcastFragButton)
    button.setOnClickListener(new OnClickListener {
      override def onClick(v: View): Unit = {
        //val intent: Intent = new Intent(getActivity, classOf[BroadcastActivity])
        //startActivity(intent)
      }
    })
    rootView
  }

  override def onResume() {
    super.onResume()
  }

  override def onPause() {
    super.onPause()
  }

  override def onClick(v: View): Unit = {

  }
}*/

class BroadcastFragment extends android.support.v4.app.Fragment{

  protected var msgListView: ListView = _
  protected var MsgChangeSub: Subscription = _
  //protected var msgView: TextView = _
  protected var msgList = new util.ArrayList[MsgItem]()
  var messageBox: EditText = _
  val MESSAGE_LENGTH_LIMIT = Constants.MAX_MESSAGE_LENGTH * 64
  var adapter: MsgListAdapter = _


  override def onCreateView(inflater: LayoutInflater, container: ViewGroup, savedInstanceState: Bundle): View = {
    val rootView = inflater.inflate(R.layout.activity_chat, container, false)
    msgListView = rootView.findViewById(R.id.msgListView).asInstanceOf[ListView]
    //msgListView.setChoiceMode(AbsListView.CHOICE_MODE_SINGLE)
    //msgView = rootView.findViewById(R.id.currentSentMsg).asInstanceOf[TextView]
    updateMsgList()

    adapter = new MsgListAdapter(getActivity.getApplicationContext, msgList)
    msgListView.setAdapter(adapter)
    adapter.notifyDataSetChanged()
    scrollToRecentItem(msgListView)

    val b = rootView.findViewById(R.id.send_message_button)
    b.setOnClickListener(new View.OnClickListener() {
      override def onClick(v: View) {
        onSendMessage()
        //updateMsgList()
      }
    })
    messageBox = rootView.findViewById(R.id.your_message).asInstanceOf[EditText]
    messageBox.setFilters(Array[InputFilter](new LengthFilter(MESSAGE_LENGTH_LIMIT)))


    /* Auxiliary functions: attachment, photo, camera */
    /*val attachmentButton = rootView.findViewById(R.id.attachment_button).asInstanceOf[Button]
    val cameraButton = rootView.findViewById(R.id.camera_button)
    val imageButton = rootView.findViewById(R.id.image_button)
    attachmentButton.setOnClickListener(new View.OnClickListener() {
      override def onClick(v: View) {}
    })
    cameraButton.setOnClickListener(new View.OnClickListener() {
      override def onClick(v: View) {}
    })
    imageButton.setOnClickListener(new View.OnClickListener() {
      override def onClick(v: View) {}
    })*/
    /* Auxiliary functions: attachment, photo, camera */

    rootView
  }

  private def updateMsgList(): Unit = {
    val db = State.db
    msgList = db.getPrivateMsgList()
  }

  private def onSendMessage() {
    // AntoxLog.debug("sendMessage")
    val mMessage = validateMessageBox()

    mMessage.foreach(rawMessage => {
      messageBox.setText("")
      val meMessagePrefix = "/me "
      val messageType = if (rawMessage.startsWith(meMessagePrefix)) ToxMessageType.ACTION else ToxMessageType.NORMAL
      val message =
        if (messageType == ToxMessageType.ACTION) {
          rawMessage.replaceFirst(meMessagePrefix, "")
        } else {
          rawMessage
        }
      //msgView.setText(message)
      //State.db.addPrivateMessage(true, message)
      msgList.add(new MsgItem(message,true,null))
      adapter.notifyDataSetChanged()
      scrollToRecentItem(msgListView)

      sendMessage(message, messageType, getActivity())
    })
  }

  def sendMessage(message: String, messageType: ToxMessageType, context: Context): Unit = {
    /*for (friendKey <- ToxSingleton.tox.getFriendList) {
      MessageHelper.sendMessage(getActivity(), friendKey, message, messageType, None)
    }*/
    MessageHelper.sendFirstMessage(getActivity, message)
  }

  def scrollToRecentItem(listView: ListView): Unit = {
    listView.post(new Runnable {
      override def run(): Unit = {
        listView.setSelection(listView.getAdapter.getCount-1)
      }
    })
  }

  /*def getActiveMessagesUpdatedObservable: Observable[Int] = {
    val db = State.db
    db.messageListUpdatedObservable()
  }


  def getActiveMessageList(takeLast: Int = -1): ArrayBuffer[MsgItem] = {
    val db = State.db
    val tmpList = db.getBroadcastMessageList(takeLast = takeLast)
    val msgItemList = new ArrayBuffer[MsgItem]()
    var msgStatus: MessageStatus = MessageStatus.Sent
    var msgString: String = ""
    var time: Timestamp = null
    var msgPiece = new MsgItem(null,null,null)
    for (msg <- tmpList) {
      if (msg.senderKey == ToxSingleton.tox.getSelfKey) {
        msgStatus = MessageStatus.Sent
      }
      else {
        msgStatus = MessageStatus.Received
      }
      msgString = msg.message
      time = msg.timestamp
      msgPiece = new MsgItem(msgStatus, msgString, time)
      msgItemList += msgPiece
    }
    msgItemList
    //return ArrayBuffer.empty
  }*/

  override def onResume(): Unit = {
    super.onResume()
    /*val db = State.db
    MsgChangeSub = db.getPrivateMsgList
      .observeOn(AndroidMainThreadScheduler())
      .subscribe((updateMsgList(_))*/
  }

  //def updateChat(messageList: Seq[MsgItem]): Unit

  def validateMessageBox(): Option[String] = {
    if (messageBox.getText != null && messageBox.getText.toString.length() == 0) {
      return None
    }

    var msg: String = null
    if (messageBox.getText != null) {
      msg = messageBox.getText.toString
    } else {
      msg = ""
    }

    Some(msg)
  }

  override def onPause() {
    super.onPause()
    //MsgChangeSub.unsubscribe()
  }

}