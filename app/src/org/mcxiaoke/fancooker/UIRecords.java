package org.mcxiaoke.fancooker;

import java.io.File;

import org.mcxiaoke.fancooker.adapter.RecordCursorAdaper;
import org.mcxiaoke.fancooker.controller.DataController;
import org.mcxiaoke.fancooker.controller.SimpleDialogListener;
import org.mcxiaoke.fancooker.dao.model.RecordColumns;
import org.mcxiaoke.fancooker.dao.model.RecordModel;
import org.mcxiaoke.fancooker.dialog.ConfirmDialog;
import org.mcxiaoke.fancooker.service.QueueService;
import org.mcxiaoke.fancooker.util.StringHelper;
import org.mcxiaoke.fancooker.util.Utils;

import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.provider.BaseColumns;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.AdapterContextMenuInfo;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;

/**
 * @author mcxiaoke
 * @version 1.0 2011.10.27
 * @version 1.1 2011.10.28
 * @version 1.2 2011.11.11
 * @version 2.0 2012.02.21
 * @version 2.1 2012.03.02
 * 
 */
public class UIRecords extends UIBaseSupport implements OnItemClickListener {
	private ListView mListView;

	private Cursor mCursor;
	private RecordCursorAdaper mAdapter;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setLayout();
	}

	@Override
	public void onCreateContextMenu(ContextMenu menu, View v,
			ContextMenuInfo menuInfo) {
		super.onCreateContextMenu(menu, v, menuInfo);
		menu.add(0, 0, 0, "删除草稿");
	}

	@Override
	public boolean onContextItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case 0:
			AdapterContextMenuInfo info = (AdapterContextMenuInfo) item
					.getMenuInfo();
			deleteRecord(info.position);
			return true;
		default:
			return super.onContextItemSelected(item);
		}
	}

	private void deleteRecord(int position) {
		final Cursor cursor = (Cursor) mAdapter.getItem(position);
		if (cursor != null) {
			int id = DataController.parseInt(cursor, BaseColumns._ID);
			int result = DataController.deleteRecord(mContext, id);
			if (result > 0) {
				// mCursor.requery();
				Utils.notify(mContext, "删除成功");
			}
		}
	}

	protected void setLayout() {
		setContentView(R.layout.list_drafts);
		setListView();

	}

	private void setListView() {
		mCursor = managedQuery(RecordColumns.CONTENT_URI, null, null, null,
				null);
		mAdapter = new RecordCursorAdaper(this, mCursor);
		mListView = (ListView) findViewById(android.R.id.list);
		mListView.setAdapter(mAdapter);
		mListView.setOnItemClickListener(this);
		registerForContextMenu(mListView);
	}

	private void onMenuClearClick() {
		DataController.clear(mContext, RecordColumns.CONTENT_URI);
		// mCursor.requery();
		Utils.notify(this, "草稿箱已清空");
		finish();
	}

	private void startTaskQueueService() {
		QueueService.start(this);
	}

	private void doSendAll() {
		final ConfirmDialog dialog = new ConfirmDialog(this);
		dialog.setTitle("提示");
		dialog.setMessage("确定发送所有草稿吗？");
		dialog.setClickListener(new SimpleDialogListener() {

			@Override
			public void onPositiveClick() {
				super.onPositiveClick();
				startTaskQueueService();
				onMenuHomeClick();
			}
		});
		dialog.show();
	}

	@Override
	public void onItemClick(AdapterView<?> parent, View view, int position,
			long id) {
		final Cursor cursor = (Cursor) parent.getItemAtPosition(position);
		if (cursor != null) {
			final RecordModel record = RecordModel.from(cursor);
			showWrite(record);
		}
	}

	private void showWrite(final RecordModel record) {
		if (record == null) {
			return;
		}

		Intent intent = new Intent(this, UIWrite.class);
		intent.putExtra("type", record.getType());
		intent.putExtra("text", record.getText());
		intent.putExtra("record_id", record.getId());
		intent.putExtra("id", record.getReply());
		if (!StringHelper.isEmpty(record.getFile())) {
			intent.putExtra("data", new File(record.getFile()));
		}
		startActivity(intent);
		finish();

	}

}
