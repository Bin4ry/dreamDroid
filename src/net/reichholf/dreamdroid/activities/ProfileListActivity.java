/* © 2010 Stephan Reichholf <stephan at reichholf dot net>
 * 
 * Licensed under the Create-Commons Attribution-Noncommercial-Share Alike 3.0 Unported
 * http://creativecommons.org/licenses/by-nc-sa/3.0/
 */

package net.reichholf.dreamdroid.activities;

import net.reichholf.dreamdroid.DreamDroid;
import net.reichholf.dreamdroid.Profile;
import net.reichholf.dreamdroid.R;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ListActivity;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;
import android.widget.Toast;

/**
 * @author sre
 * 
 */
public class ProfileListActivity extends ListActivity {
	private SimpleCursorAdapter mAdapter;
	private Profile mProfile;
	private Cursor mCursor;

	public static final int ITEM_ADD_PROFILE = 0;
	public static final int DIALOG_PROFILE_ID = 0;
	public static final int DIALOG_PROFILE_CONFIRM_DELETE_ID = 1;
	public static final int EDIT_PROFILE_REQUEST = 1;

	/*
	 * (non-Javadoc)
	 * 
	 * @see android.app.Activity#onCreate(android.os.Bundle)
	 */
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		mCursor = DreamDroid.getProfiles();
		mAdapter = new SimpleCursorAdapter(this,
				android.R.layout.two_line_list_item, mCursor, new String[] {
						DreamDroid.KEY_PROFILE, DreamDroid.KEY_HOST },
				new int[] { android.R.id.text1, android.R.id.text2 });
		
		setListAdapter(mAdapter);
		
		getListView().setOnItemLongClickListener(new OnItemLongClickListener(){
			@Override
			public boolean onItemLongClick(AdapterView<?> a, View v,
					int position, long id) {
				return onListItemLongClick(a, v, position, id);				
			}
		});
		
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see android.app.ListActivity#onListItemClick(android.widget.ListView,
	 * android.view.View, int, long)
	 */
	@Override
	protected void onListItemClick(ListView l, View v, int position, long id) {
		mProfile = new Profile(mCursor);
		activateProfile();
	}

	protected boolean onListItemLongClick(AdapterView<?> a, View v, int position, long id) {
		mProfile = new Profile(mCursor);
		showDialog(DIALOG_PROFILE_ID);
		return true;
	}
	
	/*
	 * (non-Javadoc)
	 * 
	 * @see android.app.Activity#onCreateDialog(int)
	 */
	protected Dialog onCreateDialog(int id) {
		Dialog dialog;
		switch (id) {
		case (DIALOG_PROFILE_ID):
			CharSequence[] actions = { getText(R.string.edit),
					getText(R.string.delete) };

			AlertDialog.Builder adBuilder = new AlertDialog.Builder(this);
			adBuilder.setTitle(mProfile.getProfile());
			adBuilder.setItems(actions, new DialogInterface.OnClickListener() {

				public void onClick(DialogInterface dialog, int which) {
					switch (which) {
					case 0:
						editProfile();
						break;

					case 1:
						showDialog(DIALOG_PROFILE_CONFIRM_DELETE_ID);
						break;
						
					default:
						break;
					}
				}
			});

			dialog = adBuilder.create();
			break;

		case (DIALOG_PROFILE_CONFIRM_DELETE_ID):
			AlertDialog.Builder builder = new AlertDialog.Builder(this);

			builder.setTitle(mProfile.getProfile())
					.setMessage(R.string.confirm_delete_profile)
					.setCancelable(false)
					.setPositiveButton(android.R.string.yes,
							new DialogInterface.OnClickListener() {
								public void onClick(DialogInterface dialog,
										int id) {

									if (DreamDroid.deleteProfile(mProfile)) {
										showToast(getText(R.string.profile_deleted)
												+ " '"
												+ mProfile.getProfile()
												+ "'");
									} else {
										showToast(getText(R.string.profile_not_deleted)
												+ " '"
												+ mProfile.getProfile()
												+ "'");
									}
									// TODO Add error handling
									mProfile = null;
									mCursor.requery();
									mAdapter.notifyDataSetChanged();
								}
							})
					.setNegativeButton(android.R.string.no,
							new DialogInterface.OnClickListener() {
								public void onClick(DialogInterface dialog,
										int id) {
									dialog.cancel();
								}
							});
			dialog = builder.create();
			break;
		default:
			dialog = null;
		}
		return dialog;

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see android.app.Activity#onActivityResult(int, int,
	 * android.content.Intent)
	 */
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (requestCode == EDIT_PROFILE_REQUEST) {
			if (resultCode == RESULT_OK) {
				mCursor.requery();
				mAdapter.notifyDataSetChanged();
			}
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see android.app.Activity#onCreateOptionsMenu(android.view.Menu)
	 */
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		menu.add(0, ITEM_ADD_PROFILE, 1, getText(R.string.profile_add))
				.setIcon(android.R.drawable.ic_menu_add);

		return true;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see android.app.Activity#onOptionsItemSelected(android.view.MenuItem)
	 */
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case (ITEM_ADD_PROFILE):
			createProfile();
		}
		return true;
	}

	/**
	 * 
	 */
	private void activateProfile() {
		if (DreamDroid.setActiveProfile(mProfile.getId())) {
			showToast(getText(R.string.profile_activated) + " '"
					+ mProfile.getProfile() + "'");

			finish();
		} else {
			showToast(getText(R.string.profile_not_activated) + " '"
					+ mProfile.getProfile() + "'");
		}
	}

	/**
	 * 
	 */
	private void editProfile() {
		Intent intent = new Intent(this, ProfileEditActivity.class);
		intent.putExtra("profile", mProfile);
		intent.setAction(Intent.ACTION_EDIT);

		startActivityForResult(intent, EDIT_PROFILE_REQUEST);
	}

	/**
	 * 
	 */
	private void createProfile() {
		Intent intent = new Intent(this, ProfileEditActivity.class);
		intent.setAction(Intent.ACTION_INSERT);
		startActivityForResult(intent, EDIT_PROFILE_REQUEST);
	}

	/**
	 * @param text
	 */
	protected void showToast(String text) {
		Toast toast = Toast.makeText(this, text, Toast.LENGTH_LONG);
		toast.show();
	}
}
