package com.byted.camp.todolist.db;

import android.provider.BaseColumns;

/**
 * Created on 2019/1/22.
 *
 * @author xuyingyi@bytedance.com (Yingyi Xu)
 */
public final class TodoContract {

    // TODO 定义表结构和 SQL 语句常量
    private TodoContract() {
    }
    public static class TodoEntry implements BaseColumns{
        public static final String TABLE_NAME = "TodoList";
        public static final String COLUMN_NAME_DONE = "done";
        public static final String COLUMN_NAME_TEXT = "description";
        public static final String COLUMN_NAME_DATE = "date";
        public static final String COLUMN_NAME_LEVEL = "level";
    }
    public static final String SQL_CREATE_LIST =
            "CREATE TABLE " + TodoEntry.TABLE_NAME + "(" +
                    TodoEntry._ID + " INTEGER PRIMARY KEY,"+
                    TodoEntry.COLUMN_NAME_TEXT + " TEXT,"+
                    TodoEntry.COLUMN_NAME_DATE + " DATETIME,"+
                    TodoEntry.COLUMN_NAME_LEVEL+" INT CHECK (level BETWEEN 1 AND 3),"+
                    TodoEntry.COLUMN_NAME_DONE +" TINYINT(1))";
    public static final String SQL_DELETE_ENTRY =
            "DROP TABLE IF EXISTS "+TodoEntry.TABLE_NAME;

}
