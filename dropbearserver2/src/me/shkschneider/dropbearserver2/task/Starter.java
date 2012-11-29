/*
 * Pawel Nadolski <http://stackoverflow.com/questions/10319471/android-is-the-groupid-of-sdcard-rw-always-1015/>
 */
package me.shkschneider.dropbearserver2.task;

import android.annotation.TargetApi;
import android.app.ProgressDialog;
import android.content.Context;
import android.os.AsyncTask;
import android.os.Build;

import me.shkschneider.dropbearserver2.LocalPreferences;
import me.shkschneider.dropbearserver2.util.L;
import me.shkschneider.dropbearserver2.util.ServerUtils;
import me.shkschneider.dropbearserver2.util.ShellUtils;

public class Starter extends AsyncTask<Void, String, Boolean> {

	private static final int ID_ROOT = 0;

	private Context mContext = null;
	private ProgressDialog mProgressDialog = null;
	private Boolean mStartInBackground = false;

	private Callback<Boolean> mCallback = null;

	public Starter(Context context, Callback<Boolean> callback) {
		this(context, callback, false);
	}

	public Starter(Context context, Callback<Boolean> callback, Boolean startInBackground) {
		mContext = context;
		mCallback = callback;
		mStartInBackground = startInBackground;

		if (mContext != null && !mStartInBackground) {
			mProgressDialog = new ProgressDialog(mContext);
			mProgressDialog.setTitle("Starter");
			mProgressDialog.setMessage("Please wait...");
			mProgressDialog.setCancelable(false);
			mProgressDialog.setCanceledOnTouchOutside(false);
			mProgressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
			mProgressDialog.setMax(100);
			mProgressDialog.setIcon(0);
		}
	}

	@Override
	protected void onPreExecute() {
		super.onPreExecute();
		if (mProgressDialog != null && !mStartInBackground) {
			mProgressDialog.show();
		}
	}

	private Boolean falseWithError(String error) {
		L.d(error);
		return false;
	}

	@Override
	protected Boolean doInBackground(Void... params) {
		String login = "root";
		String passwd = LocalPreferences.getString(mContext, LocalPreferences.PREF_PASSWORD, LocalPreferences.PREF_PASSWORD_DEFAULT);
		String banner = ServerUtils.getLocalDir(mContext) + "/banner";
		String hostRsa = ServerUtils.getLocalDir(mContext) + "/host_rsa";
		String hostDss = ServerUtils.getLocalDir(mContext) + "/host_dss";
		String authorizedKeys = ServerUtils.getLocalDir(mContext) + "/authorized_keys";
		Integer listeningPort = 22;
		String pidFile = ServerUtils.getLocalDir(mContext) + "/pid";

		String command = ServerUtils.getLocalDir(mContext) + "/dropbear";
		command = command.concat(" -A -N " + login);
		if (LocalPreferences.getBoolean(mContext, LocalPreferences.PREF_ALLOW_PASSWORD, LocalPreferences.PREF_ALLOW_PASSWORD_DEFAULT) == true) {
			command = command.concat(" -C " + passwd);
		}
		else {
			command = command.concat(" -s");
		}
		command = command.concat(" -r " + hostRsa + " -d " + hostDss);
		command = command.concat(" -R " + authorizedKeys);
		command = command.concat(" -U " + ID_ROOT + " -G " + ID_ROOT);
		command = command.concat(" -p " + listeningPort);
		command = command.concat(" -P " + pidFile);
		command = command.concat(" -b " + banner);

		L.d("Command: " + command);
		if (ShellUtils.execute(command) == false) {
			return falseWithError("execute(" + command + ")");
		}

		return true;
	}

	@Override
	protected void onPostExecute(Boolean result) {
		dismiss();

		if (mCallback != null) {
			mCallback.onTaskComplete(Callback.TASK_START, result);
		}
	}

	@Override
	protected void onCancelled() {
		dismiss();

		super.onCancelled();
	}

	@TargetApi(Build.VERSION_CODES.HONEYCOMB)
	@Override
	protected void onCancelled(Boolean result) {
		dismiss();

		super.onCancelled(result);
	}

	private void dismiss() {
		try {
			if (mProgressDialog != null) {
				mProgressDialog.dismiss();
				mProgressDialog = null;
			}
		}
		catch (IllegalArgumentException e) {
			L.w("IllegalArgumentException: " + e.getMessage());
		}
	}
}