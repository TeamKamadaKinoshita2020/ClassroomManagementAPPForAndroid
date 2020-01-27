package jp.ac.ibaraki.felicacardidlinkapp;

import android.app.Activity;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.Context;
import android.content.DialogInterface;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;


/**
 * カードIDリンク時に表示されるダイヤログのフラグメント
 */
public class ReadCardIdFragment extends DialogFragment {

    public interface CommonDialogInterface {
        public interface onClickListener {
            void onDialogButtonClick(String tag, Dialog dialog, int which);
        }

        public interface onShowListener {
            void onDialogShow(String tag, Dialog dialog);
        }

        public interface onItemClickListener {
            void onDialogItemClick(String tag, Dialog dialog, String title, int which);
        }
    }

    public static final String FIELD_LAYOUT = "layout";
    public static final String FIELD_TITLE = "title";
    public static final String FIELD_MESSAGE = "message";
    public static final String FIELD_LIST_ITEMS = "list_items";
    public static final String FIELD_LIST_ITEMS_STRING = "list_items_string";
    public static final String SELECT_CARD_ID = "select_card_id";
    public static final String READ_CARD_ID = "read_card_id";
    public static final String SELECT_CARD_IDENTITY_ID = "select_card_identity_id";
    public static final String READ_CARD_IDENTITY_ID = "read_card_identity_id";
    public static final String FIELD_LABEL_POSITIVE = "label_positive";
    public static final String FIELD_LABEL_NEGATIVE = "label_negative";
    public static final String FIELD_LABEL_NEUTRAL = "label_neutral";

    private CommonDialogInterface.onShowListener mListenerShow;
    private CommonDialogInterface.onClickListener mListenerOnClick;
    private CommonDialogInterface.onItemClickListener mListenerItemClick;
    private AlertDialog mAlertDialog;

    // Activityへイベントを送るためのリスナー
    public interface MyListener {
        public void onClickButton(boolean positiveFlag);
    }
    private MyListener mListener;

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {

        Bundle args = getArguments();

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

        mListener = ((MyListener)getActivity());


        // listener check
        if (getTargetFragment() != null) {
            setListener(getTargetFragment());
        } else if (getActivity() != null) {
            setListener(getActivity());
        }

        // dialog title
        if (args.containsKey(FIELD_TITLE)) {
            builder.setTitle(args.getString(FIELD_TITLE));
        }

        // dialog message
        if (args.containsKey(FIELD_MESSAGE)) {
            builder.setMessage(args.getString(FIELD_MESSAGE));
        }

        // dialog customize content view ダイヤログのレイアウト設定
        if (args.containsKey(FIELD_LAYOUT) && args.containsKey(SELECT_CARD_ID) && args.containsKey(READ_CARD_ID)) {
            LayoutInflater inflater = (LayoutInflater)getActivity().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            View content = inflater.inflate(args.getInt(FIELD_LAYOUT), null);
            TextView tv = (TextView)content.findViewById(R.id.beforeIdText);
            tv.setText("現在のカード:" + args.getString(SELECT_CARD_IDENTITY_ID));
            TextView tv2 = (TextView)content.findViewById(R.id.newIdText);
            tv2.setText("新しいカード:" + args.getString(READ_CARD_IDENTITY_ID));
            builder.setView(content);
        }

        // dialog string list
        final List<String> items = new ArrayList<String>();
        if (args.containsKey(FIELD_LIST_ITEMS)) {
            final int[] listItems = args.getIntArray(FIELD_LIST_ITEMS);
            for (int i = 0; i < listItems.length; i++) {
                items.add(getString(listItems[i]));
            }
        }
        if (args.containsKey(FIELD_LIST_ITEMS_STRING)) {
            final String[] listItems = args.getStringArray(FIELD_LIST_ITEMS_STRING);
            for (int i = 0; i < listItems.length; i++) {
                items.add(listItems[i]);
            }
        }
        if (items.size() > 0) {
            builder.setItems(items.toArray(new String[items.size()]), new DialogInterface.OnClickListener() {

                @Override
                public void onClick(DialogInterface dialog, int which) {
                    if ( mListenerItemClick != null) {
                        mListenerItemClick.onDialogItemClick(getTag(), mAlertDialog, items.get(which), which);
                    }
                }

            });
        }


        if (getActivity() instanceof MyListener == false) {
            throw new ClassCastException("activity が OnOkBtnClickListener を実装していません.");
        }



        // positive button title and click listener　はいの場合？
        if (args.containsKey(FIELD_LABEL_POSITIVE)) {
            builder.setPositiveButton(args.getString(FIELD_LABEL_POSITIVE), new DialogInterface.OnClickListener() {

                @Override
                public void onClick(DialogInterface dialog, int which) {
                    Log.d("Yes","yes");
                    mListener.onClickButton(true);//Activityにイベントを送る
                    if (mListenerOnClick != null) {
                        mListenerOnClick.onDialogButtonClick(getTag(), mAlertDialog, which);
                    }

                }
            });
        }

        // negative button title and click listener いいえの場合？
        if (args.containsKey(FIELD_LABEL_NEGATIVE)) {
            builder.setNegativeButton(args.getString(FIELD_LABEL_NEGATIVE), new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int which) {
                    Log.d("No","no");
                    mListener.onClickButton(false);//Activityにイベントを送る
                    if (mListenerOnClick != null) {
                        mListenerOnClick.onDialogButtonClick(getTag(), mAlertDialog, which);
                    }
                }
            });
        }

        // neutral button title and click listener
        if (args.containsKey(FIELD_LABEL_NEUTRAL)) {
            builder.setNeutralButton(args.getString(FIELD_LABEL_NEUTRAL), new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    if (mListenerOnClick != null) {
                        mListenerOnClick.onDialogButtonClick(getTag(), mAlertDialog, which);
                    }
                }
            });
        }

        // make dialog
        mAlertDialog = builder.create();

        // show listener
        if (mListenerShow != null) {
            mAlertDialog.setOnShowListener(new DialogInterface.OnShowListener() {

                @Override
                public void onShow(DialogInterface dialog) {
                    mListenerShow.onDialogShow(getTag(), mAlertDialog);
                }
            });
        }

        return mAlertDialog;
    }

    private void setListener(Object target) {

        // on click listener
        if (target instanceof CommonDialogInterface.onClickListener) {
            mListenerOnClick = (CommonDialogInterface.onClickListener) target;
        }

        // on item click listener
        if (target instanceof CommonDialogInterface.onItemClickListener) {
            mListenerItemClick = (CommonDialogInterface.onItemClickListener) target;
        }

        // on show listener
        if (target instanceof CommonDialogInterface.onShowListener) {
            mListenerShow = (CommonDialogInterface.onShowListener) target;
        }

    }

}