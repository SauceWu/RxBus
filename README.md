#injectExtra 


```java
public class ExampleActivity extends AppCompatActivity {
  private UnSubscribe unSubscribe;
  
    @Override
    protected void onResume() {
        super.onResume();
        unSubscribe = RxManager.init(this);
    }

    @Override
    protected void onPause() {
        super.onPause();
        unSubscribe.unSubscribe();
    }
    @Subscribe(tag= 101,thread = EventThread.MAIN_THREAD)
    public void onEvent(Event errorMsg) {

    }
}

```
Download
--------

```groovy


allprojects {
    repositories {
     jcenter()
    }

}



dependencies {
   compile  "me.sauce:rxBus:1.0.3"
   annotationProcessor "me.sauce:rxBus-compiler:1.0.3"
}
```