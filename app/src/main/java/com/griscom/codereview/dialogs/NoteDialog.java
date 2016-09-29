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
import android.view.WindowManager;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;

import com.griscom.codereview.R;
import com.griscom.codereview.other.ApplicationPreferences;

import java.util.ArrayList;

/**
 * Dialog for creating notes
 */
public class NoteDialog extends DialogFragment
{
    @SuppressWarnings("unused")
    private static final String TAG = "NoteDialog";



    private static final String ARG_ITEMS = "ITEMS";
    private static final String ARG_NOTE  = "NOTE";



    private OnFragmentInteractionListener mListener      = null;
    private ArrayList<Integer>            mItems         = null;
    private String                        mNote          = null;
    private ArrayList<String>             mNotes         = null;



    /**
     * Creates new instance of NoteDialog with pre-entered note
     * @param items     indices in the list
     * @param note      note
     * @return NoteDialog instance
     */
    public static NoteDialog newInstance(ArrayList<Integer> items, String note)
    {
        NoteDialog fragment = new NoteDialog();

        Bundle args = new Bundle();
        args.putIntegerArrayList(ARG_ITEMS, items);
        args.putString(          ARG_NOTE,  note);
        fragment.setArguments(args);

        return fragment;
    }

    /** {@inheritDoc} */
    @Override
    public void onCreate(@Nullable Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);

        mItems = getArguments().getIntegerArrayList(ARG_ITEMS);
        mNote  = getArguments().getString(          ARG_NOTE);

        loadLastNotes();
    }

    /** {@inheritDoc} */
    @Override
    @NonNull
    public Dialog onCreateDialog(Bundle savedInstanceState)
    {
        final AutoCompleteTextView editText = new AutoCompleteTextView(getActivity());
        editText.setText(mNote);
        editText.selectAll();
        editText.setAdapter(new ArrayAdapter<>(getActivity().getApplicationContext(), android.R.layout.simple_dropdown_item_1line, mNotes));
        editText.setThreshold(0);



        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

        builder.setTitle(R.string.dialog_input_note_title)
                .setMessage(R.string.dialog_input_note_message)
                .setCancelable(true)
                .setView(editText)
                .setPositiveButton(android.R.string.ok,
                        new DialogInterface.OnClickListener()
                        {
                            @Override
                            public void onClick(DialogInterface dialog, int whichButton)
                            {
                                String note = editText.getText().toString();

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
    private void saveLastNotes()
    {
        SharedPreferences prefs = getActivity().getSharedPreferences(ApplicationPreferences.NOTES_SHARED_PREFERENCES, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();

        editor.putInt(ApplicationPreferences.LAST_NOTES, mNotes.size());

        for (int i = 0; i < mNotes.size(); ++i)
        {
            editor.putString(ApplicationPreferences.ONE_NOTE + "_" + String.valueOf(i + 1), mNotes.get(i));
        }

        editor.apply();
    }

    /**
     * Loads last entered notes
     */
    private void loadLastNotes()
    {
        mNotes = new ArrayList<>();

        SharedPreferences prefs = getActivity().getSharedPreferences(ApplicationPreferences.NOTES_SHARED_PREFERENCES, Context.MODE_PRIVATE);
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
        void onNoteEntered(ArrayList<Integer> items, String note);
    }
}
