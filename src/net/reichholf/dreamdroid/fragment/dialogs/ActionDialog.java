/* © 2010 Stephan Reichholf <stephan at reichholf dot net>
 * 
 * Licensed under the Create-Commons Attribution-Noncommercial-Share Alike 3.0 Unported
 * http://creativecommons.org/licenses/by-nc-sa/3.0/
 */

package net.reichholf.dreamdroid.fragment.dialogs;

/**
 * @author sre
 * 
 */
public abstract class ActionDialog extends AbstractDialog {
	protected void finishDialog(int action) {
		((DialogActionListener) getActivity()).onDialogAction(action);
		dismiss();
	}

	public interface DialogActionListener {
		public void onDialogAction(int action);
	}

}
