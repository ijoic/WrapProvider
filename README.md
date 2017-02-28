# Wrap Provider

Easy access for creating `ContentProvider`. And supports multiple table management inside one `ContentProvider`.

## Usage

1. Extends `BaseContentProvider`.

     ```java

    public class MyProvider extends BaseContentProvider {
      @NonNull
      @Override
      protected ProviderHelper genProviderHelper() {
        return new MyProviderHelper();
      }

      @Override
      protected void initDatabaseConfig(@NonNull DatabaseConfig databaseConfig) {
        databaseConfig.authority = "com.example.app";
        databaseConfig.databaseName = "my_database";
        databaseConfig.databaseVersion = 1;
      }
    }

     ```
2. Extends `BaseProviderHelper`.

    ```java

    public class MyProviderHelper extends BaseProviderHelper {
      @Override
      protected void onInitTableConfig(TableConfig config) {
        config.contentUri = null;
        // ..
      }

      @Override
      protected void onCreateDatabase(@NonNull SQLiteDatabase db) {
        db.execSQL("CREATE TABLE ..;");
      }
    }

    ```

3. Declare `Provider` inside `AndroidManifest.xml`.

    ```xml
    <manifest>
      <application>
        <Provider
           android:name="com.example.db.MyProvider"
           android:authorities="com.example.app.MyProvider"
           android:exported="false"/>
      </application>
    </manifest>
    ```