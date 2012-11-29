package me.shkschneider.dropbearserver2.task;

import android.annotation.TargetApi;
import android.app.ProgressDialog;
import android.content.Context;
import android.os.AsyncTask;
import android.os.Build;

import me.shkschneider.dropbearserver2.util.L;
import me.shkschneider.dropbearserver2.util.RootUtils;
import me.shkschneider.dropbearserver2.util.ServerUtils;

public class Checker extends AsyncTask<Void, String, Boolean> {

	private Context mContext = null;
	private ProgressDialog mProgressDialog = null;

	private Callback<Boolean> mCallback = null;

	public Checker(Context context, Callback<Boolean> callback) {
		mContext = context;
		mCallback = callback;

		if (mContext != null) {
			mProgressDialog = new ProgressDialog(mContext);
			mProgressDialog.setTitle("Checker");
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
		if (mProgressDialog != null) {
			mProgressDialog.show();
		}
	}

	@Override
	protected void onProgressUpdate(String... progress) {
		super.onProgressUpdate(progress);
		if (mProgressDialog != null) {
			Float f = (Float.parseFloat(progress[0] + ".0") / Float.parseFloat(progress[1] + ".0") * 100);
			mProgressDialog.setProgress(Math.round(f));
			mProgressDialog.setMessage(progress[2]);
		}
	}

	@Override
	protected Boolean doInBackground(Void... params) {
		int step = 0;
		int steps = 5;

		// root
		publishProgress("" + step++, "" + steps, "Root access");
		RootUtils.checkRootAccess();

		// busybox
		publishProgress("" + step++, "" + steps, "Busybox");
		RootUtils.checkBusybox();

		// dropbear
		publishProgress("" + step++, "" + steps, "DropBear");
		RootUtils.checkDropbear(mContext);

		ServerUtils.isDropbearRunning();

		ServerUtils.getIpAddresses(mContext);

		ServerUtils.getDropbearVersion(mContext);

		return (RootUtils.hasRootAccess && RootUtils.hasBusybox && RootUtils.hasDropbear && ServerUtils.dropbearRunning);
	}

	@Override
	protected void onPostExecute(Boolean result) {
		dismiss();

		if (mCallback != null) {
			mCallback.onTaskComplete(Callback.TASK_CHECK, result);
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