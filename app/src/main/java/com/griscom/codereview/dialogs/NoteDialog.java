package com.griscom.codereview.dialogs;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;

import com.griscom.codereview.R;
import com.griscom.codereview.other.ApplicationPreferences;

import java.util.ArrayList;

/**
 * Dialog for creating notes
 */
public class NoteDialog extends DialogFragment implements View.OnClickListener
{
    @SuppressWarnings("unused")
    private static final String TAG = "NoteDialog";



    private static final String NOTES_SHARED_PREFERENCES = "Notes";



    private static final String ARG_ITEMS = "ITEMS";
    private static final String ARG_NOTE  = "NOTE";



    private OnFragmentInteractionListener mListener      = null;
    private EditText                      mInputEditText = null;
    private ImageButton                   mChooseButton  = null;
    private int[]                         mItems         = null;
    private String                        mNote          = null;
    private ArrayList<CharSequence>       mNotes         = null;



    /**
     * Creates new instance of NoteDialog with pre-entered note
     * @param items     indices in the list
     * @param note      note
     * @return NoteDialog instance
     */
    public static NoteDialog newInstance(int[] items, String note)
    {
        NoteDialog fragment = new NoteDialog();

        Bundle args = new Bundle();
        args.putIntArray(ARG_ITEMS, items);
        args.putString(  ARG_NOTE,  note);
        fragment.setArguments(args);

        return fragment;
    }

    /** {@inheritDoc} */
    @Override
    public void onCreate(@Nullable Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);

        mItems = getArguments().getIntArray(ARG_ITEMS);
        mNote  = getArguments().getString(  ARG_NOTE);

        loadLastNotes();
    }

    /** {@inheritDoc} */
    @Override
    @NonNull
    public Dialog onCreateDialog(Bundle savedInstanceState)
    {
        LayoutInflater inflater = getActivity().getLayoutInflater();

        View rootView = inflater.inflate(R.layout.dialog_input, null, false);



        mInputEditText = (EditText)   rootView.findViewById(R.id.inputEditText);
        mChooseButton  = (ImageButton)rootView.findViewById(R.id.chooseButton);



        mInputEditText.setText(mNote);

        mChooseButton.setOnClickListener(this);



        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

        builder.setTitle(R.string.dialog_input_note_title)
                .setMessage(R.string.dialog_input_note_message)
                .setCancelable(true)
                .setView(rootView)
                .setPositiveButton(android.R.string.ok,
                        new DialogInterface.OnClickListener()
                        {
                            @Override
                            public void onClick(DialogInterface dialog, int whichButton)
                            {
                                String note = mInputEditText.getText().toString();

                                if (!note.equals(""))
                                {
                                    mNotes.remove(note);
                                    mNotes.add(0, note);

                                    saveLastNotes();
                                }

                                onNoteEntered(note);

                                dialog.dismiss();
                            }
                        })
                .setNegativeButton(android.R.string.cancel,
                        new DialogInterface.OnClickListener()
                        {
                            @Override
                            public void onClick(DialogInterface dialog, int whichButton)
                            {
                                dialog.dismiss();
                            }
                        });

        AlertDialog dialog = builder.create();
        dialog.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE);

        return dialog;
    }

    /** {@inheritDoc} */
    @Override
    public void onClick(View view)
    {
        if (view == mChooseButton)
        {
            if (mNotes.size() > 0)
            {
                final CharSequence items[] = new CharSequence[mNotes.size()];
                String currentNote = mInputEditText.getText().toString();
                int index = -1;

                for (int i = 0; i < mNotes.size(); ++i)
                {
                    String oneNote = (String)mNotes.get(i);

                    if (index < 0 && oneNote.equals(currentNote))
                    {
                        index = i;
                    }

                    items[i] = oneNote;
                }

                if (index < 0)
                {
                    index = 0;
                }

                AlertDialog chooseDialog = new AlertDialog.Builder(getActivity())
                        .setSingleChoiceItems(items, index, new DialogInterface.OnClickListener()
                        {
                            @Override
                            public void onClick(DialogInterface dialog, int index)
                            {
                                mInputEditText.setText(items[index]);

                                dialog.dismiss();
                            }
                        }).create();

                chooseDialog.show();
            }
            else
            {
                Toast.makeText(getActivity(), R.string.no_last_notes, Toast.LENGTH_SHORT).show();
            }
        }
        else
        {
            Log.e(TAG, "Unknown view: " + String.valueOf(view));
        }
    }

    /**
     * Handler for note entered event
     * @param note    note
     */
    public void onNoteEntered(String note)
    {
        if (mListener != null)
        {
            mListener.onNoteEntered(mItems, note);
        }
    }

    /** {@inheritDoc} */
    @Override
    public void onAttach(Context context)
    {
        super.onAttach(context);

        if (context instanceof OnFragmentInteractionListener)
        {
            mListener = (OnFragmentInteractionListener)context;
        }
        else
        {
            throw new RuntimeException(context.toString() + " must implement OnFragmentInteractionListener");
        }
    }

    /** {@inheritDoc} */
    @Override
    public void onDetach()
    {
        super.onDetach();

        mListener = null;
    }

    /**
     * Saves last entered notes
     */
    public void saveLastNotes()
    {
        SharedPreferences prefs = getActivity().getSharedPreferences(NOTES_SHARED_PREFERENCES, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();

        editor.putInt(ApplicationPreferences.LAST_NOTES, mNotes.size());

        for (int i = 0; i < mNotes.size(); ++i)
        {
            editor.putString(ApplicationPreferences.ONE_NOTE + "_" + String.valueOf(i + 1), mNotes.get(i).toString());
        }

        editor.apply();
    }

    /**
     * Loads last entered notes
     */
    public void loadLastNotes()
    {
        mNotes = new ArrayList<>();

        SharedPreferences prefs = getActivity().getSharedPreferences(NOTES_SHARED_PREFERENCES, Context.MODE_PRIVATE);
        int noteCount = prefs.getInt(ApplicationPreferences.LAST_NOTES, 0);

        for (int i = 0; i < noteCount; ++i)
        {
            String note = prefs.getString(ApplicationPreferences.ONE_NOTE + "_" + String.valueOf(i + 1),"");

            if (
                !TextUtils.isEmpty(note)
                &&
                !mNotes.contains(note)
               )
            {
                mNotes.add(note);
            }
        }
    }



    /**
     * Listener for interaction with this dialog fragment
     */
    public interface OnFragmentInteractionListener
    {
        /**
         * Handler for note entered event
         * @param items   indices in the list
         * @param note    note
         */
        void onNoteEntered(int[] items, String note);
    }
}
