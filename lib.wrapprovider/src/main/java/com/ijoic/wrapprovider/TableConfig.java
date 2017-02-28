package com.ijoic.wrapprovider;

import android.net.Uri;

/**
 * Database Table Config.
 *
 * @author ijoic 963505345@qq.com
 * @version 1.0
 */
public class TableConfig {
  /**
   * Table Name.
   */
  public String tableName;

  /**
   * Default sort order.
   */
  public String defaultSortOrder;

  /**
   * ContentUri for table.
   *
   * <p>Something like "content://com.example.app.MyProvider/notes". May not be <code>null</code>.</p>
   */
  public Uri contentUri;

  /**
   * Content Type for item set.
   *
   * <p>Something like "vnd.android.cursor.dir/vnd.example.app.note". May not be <code>null</code>.</p>
   */
  public String contentType;

  /**
   * Content Type for item.
   *
   * <p>Something like "vnd.android.cursor.item/vnd.example.app.note". May not be <code>null</code>.</p>
   */
  public String contentItemType;

  /**
   * Default null column.
   */
  public String hackColumn;

  /**
   * All table columns.
   */
  public String[] columns;

  /**
   * Table set for uri matcher.
   *
   * <p>Something like "notes". May not be <code>null</code>.</p>
   */
  public String itemSet;
}
