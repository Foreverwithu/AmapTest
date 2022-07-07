package com.example.amaptest;


import android.Manifest;


import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;

import android.util.Log;

import android.widget.TextView;
import android.widget.Toast;


import androidx.appcompat.app.AppCompatActivity;

import com.amap.api.location.AMapLocation;
import com.amap.api.location.AMapLocationClient;
import com.amap.api.location.AMapLocationClientOption;

import com.amap.api.location.AMapLocationListener;
import com.amap.api.maps.AMap;
import com.amap.api.maps.LocationSource;
import com.amap.api.maps.MapView;
import com.amap.api.maps.UiSettings;
import com.amap.api.maps.model.BitmapDescriptorFactory;
import com.amap.api.maps.model.MyLocationStyle;
import com.amap.api.services.core.PoiItem;
import com.amap.api.services.poisearch.PoiResult;
import com.amap.api.services.poisearch.PoiSearch;
import com.amap.api.services.weather.LocalWeatherLive;
import com.amap.api.services.weather.LocalWeatherLiveResult;
import com.amap.api.services.weather.WeatherSearch;
import com.amap.api.services.weather.WeatherSearchQuery;

import pub.devrel.easypermissions.EasyPermissions;

public class MainActivity extends AppCompatActivity implements AMapLocationListener, LocationSource, PoiSearch.OnPoiSearchListener {
    //请求权限码
    private static final int REQUEST_PERMISSIONS = 9527;
    private static final String TAG = "AmapTest";
    //声明AMapLocationClient类对象
    public AMapLocationClient mLocationClient = null;
    //声明AMapLocationClientOption类对象
    public AMapLocationClientOption mLocationOption = null;
    //声明地图控件
    private MapView mMapView = null;
    //地图控制器
    private AMap aMap = null;
    //声明文本控件
    private TextView tvContext;
    //设置定位蓝点格式
    private MyLocationStyle myLocationStyle;
    //设置定位监听
    private LocationSource.OnLocationChangedListener mListener;
    //设置蓝点格式和界面标尺等
    private UiSettings mUiSetting;

    private WeatherSearchQuery mquery;
    private WeatherSearch mweathersearch;
//keyWord表示搜索字符串，
//第二个参数表示POI搜索类型，二者选填其一，选用POI搜索类型时建议填写类型代码，码表可以参考下方（而非文字）
//cityCode表示POI搜索区域，可以是城市编码也可以是城市名称，也可以传空字符串，空字符串代表全国在全国范围内进行搜索


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d(TAG, "The activity state---->onCreate");
        setContentView(R.layout.activity_main);
        AMapLocationClient.updatePrivacyShow(this, true, true);
        AMapLocationClient.updatePrivacyAgree(this, true);
//        tvContext = findViewById(R.id.position_text_view);


        initLocation(); //初始化定位

        initMap(savedInstanceState);//初始化地图
        checkingAndroidVersion();//查看Android版本


    }

    /**
     * 初始化地图
     */

    private void initMap(Bundle savedInstanceState) {

        mMapView = (MapView) findViewById(R.id.map);//获取地图控件引用

        mMapView.onCreate(savedInstanceState); //在activity执行onCreate时执行mMapView.onCreate(savedInstanceState)，创建地图

        aMap = mMapView.getMap(); //初始化地图控制器对象

        aMap.setMinZoomLevel(17); //地图缩放级别为【3.20】


        aMap.setLocationSource(this); // 设置定位监听

        aMap.setMyLocationEnabled(true);// 设置为true表示显示定位层并可触发定位，false表示隐藏定位层并不可触发定位，默认是false

        aMap.setMyLocationType(AMap.LOCATION_TYPE_LOCATE);// 设置定位的类型为定位模式，有定位、跟随或地图根据面向方向旋转几种

        mUiSetting = aMap.getUiSettings();//实例化控件交互

        mUiSetting.setZoomControlsEnabled(false); //隐藏缩放按钮
        mUiSetting.setScaleControlsEnabled(true);// 可触发定位并显示当前位置

        mUiSetting.setMyLocationButtonEnabled(true); //显示默认的定位按钮(当前位置的按钮)

        myLocationStyle = new MyLocationStyle();
        // 自定义定位蓝点图标
        myLocationStyle.myLocationIcon(BitmapDescriptorFactory.fromResource(R.drawable.gps_point));
        // 自定义精度范围的圆形边框颜色  都为0则透明
        myLocationStyle.strokeColor(Color.argb(0, 0, 0, 0));
        // 自定义精度范围的圆形边框宽度  0 无宽度
        myLocationStyle.strokeWidth(10);
        // 设置圆形的填充颜色  都为0则透明
        myLocationStyle.radiusFillColor(Color.argb(50, 0, 100, 100));

        aMap.setMyLocationStyle(myLocationStyle);//设置定位蓝点的style

//        setPointToCenter(int x, int y);//x、y均为屏幕坐标，屏幕左上角为坐标原点，即(0,0)点。


    }

    /**
     * 初始化定位
     */
    private void initLocation() {
        //初始化定位

        try {


            mLocationClient = new AMapLocationClient(getApplicationContext());
        } catch (Exception e) {
            e.printStackTrace();
        }


        //设置定位回调监听
        mLocationClient.setLocationListener(this);
        //初始化AMapLocationClientOption对象
        mLocationOption = new AMapLocationClientOption();
        //设置定位模式为AMapLocationMode.Hight_Accuracy，高精度模式。
        mLocationOption.setLocationMode(AMapLocationClientOption.AMapLocationMode.Hight_Accuracy);
        //获取最近3s内精度最高的一次定位结果：
        //设置setOnceLocationLatest(boolean b)接口为true，启动定位时SDK会返回最近3s内精度最高的一次定位结果。如果设置其为true，setOnceLocation(boolean b)接口也会被设置为true，反之不会，默认为false。
        mLocationOption.setOnceLocation(true);
        //设置是否返回地址信息（默认返回地址信息）
        mLocationOption.setNeedAddress(true);
        //设置定位请求超时时间，单位是毫秒，默认30000毫秒，建议超时时间不要低于8000毫秒。
        mLocationOption.setHttpTimeOut(20000);
        //关闭缓存机制，高精度定位会产生缓存。
        mLocationOption.setLocationCacheEnable(false);
        //给定位客户端对象设置定位参数
        mLocationClient.setLocationOption(mLocationOption);
        // 此方法为每隔固定时间会发起一次定位请求，为了减少电量消耗或网络流量消耗，
        // 注意设置合适的定位时间的间隔（最小间隔支持为2000ms），并且在合适时间调用stopLocation()方法来取消定位请求
        // 在定位结束后，在合适的生命周期调用onDestroy()方法
        // 在单次定位情况下，定位无论成功与否，都无需调用stopLocation()方法移除请求，定位sdk内部会移除


        mLocationClient.startLocation();//启动定位

    }

/*
    */
/**
     * 实时天气查询回调
     *//*

    @Override
    public void onWeatherLiveSearched(LocalWeatherLiveResult weatherLiveResult, int rCode) {
        if (rCode == 1000) {
            if (weatherLiveResult != null && weatherLiveResult.getLiveResult() != null) {
                LocalWeatherLive weatherlive = weatherLiveResult.getLiveResult();
                reporttime1.setText(weatherlive.getReportTime() + "发布");
                weather.setText(weatherlive.getWeather());
                Temperature.setText(weatherlive.getTemperature() + "°");
                wind.setText(weatherlive.getWindDirection() + "风     " + weatherlive.getWindPower() + "级");
                humidity.setText("湿度         " + weatherlive.getHumidity() + "%");
            } else {
                ToastUtil.show(WeatherSearchActivity.this, R.string.no_result);
            }
        } else {
            ToastUtil.showerror(WeatherSearchActivity.this, rCode);
        }
    }
*/

    /**
     * 地图生存周期管理
     */
    @Override
    protected void onStart() {
        super.onStart();
        Log.d(TAG, "The activity state---->onStart");
    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.d(TAG, "The activity state---->onResume");
        //在activity执行onResume时执行mMapView.onResume ()，重新绘制加载地图
        mMapView.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
        Log.d(TAG, "The activity state---->onPause");
        //在activity执行onPause时执行mMapView.onPause ()，暂停地图的绘制
        mMapView.onPause();
    }

    @Override
    protected void onStop() {
        super.onStop();
        Log.d(TAG, "The activity state---->onStop");
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Log.d(TAG, "The activity state---->onDestroy");
        mMapView.onDestroy();
        if (null != mLocationClient) {
            mLocationClient.onDestroy();
        }
    }


    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        //在activity执行onSaveInstanceState时执行mMapView.onSaveInstanceState (outState)，保存地图当前的状态
        mMapView.onSaveInstanceState(outState);
    }

    @Override
    public void activate(OnLocationChangedListener onLocationChangedListener) {
        mListener = onLocationChangedListener;
        if (mLocationClient == null) {

            mLocationClient.startLocation();//启动定位
        }
    }

    /**
     * 停止定位
     */

    public void deactivate() {
        mListener = null;
        if (mLocationClient != null) {
            mLocationClient.stopLocation();
            mLocationClient.onDestroy();
        }
        mLocationClient = null;
    }


    /**
     * 检查Android版本
     */
    private void checkingAndroidVersion() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            // Android6.0及以上先获取权限再定位
            requestPermission();
        } else {
            //在6.0以下直接定位
            mLocationClient.startLocation();
        }
    }

    /**
     * 动态请求权限
     */
    private void requestPermission() {
        //所需要的权限  网络定位 GPS定位  获取手机状态权限  写入缓存到sd卡
        String[] permission = {
                Manifest.permission.ACCESS_COARSE_LOCATION,
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.READ_PHONE_STATE,
                Manifest.permission.WRITE_EXTERNAL_STORAGE
        };
        if (EasyPermissions.hasPermissions(this, permission)) {

            Log.d(TAG, "已经获得权限，可以开始使用定位了");
            //启动定位
            mLocationClient.startLocation();
        } else {
            //无权限则申请权限  权限
            EasyPermissions.requestPermissions(this, "需要权限", REQUEST_PERMISSIONS);
        }


    }


    @Override
    public void onLocationChanged(AMapLocation aMapLocation) {
        if (mListener != null && aMapLocation != null) {
            if (aMapLocation != null && aMapLocation.getErrorCode() == 0) {

                mListener.onLocationChanged(aMapLocation);
                // 停止定位后，本地服务不会被销毁
                //获取定位地址
                StringBuffer stringBuffer = new StringBuffer();
                stringBuffer.append("位置：" + aMapLocation.getAddress() + "\n"
                        + "经度：" + String.valueOf(aMapLocation.getLatitude()) + "\n"
                        + "纬度：" + String.valueOf(aMapLocation.getLongitude()) + "\n"
                        + "国家: " + String.valueOf(aMapLocation.getCountry()) + "\n"
                        + "省份：" + String.valueOf(aMapLocation.getProvince()) + "\n"
                        + "城市：" + String.valueOf(aMapLocation.getCity()) + "\n"
                        + "区域: " + String.valueOf(aMapLocation.getDistrict()) + "\n"
                        + "街道 " + String.valueOf(aMapLocation.getStreet()) + "\n");
                Log.e("位置：", aMapLocation.getAddress());
                Log.e("经度：", String.valueOf(aMapLocation.getLatitude()));
                Log.e("纬度：", String.valueOf(aMapLocation.getLongitude()));


                Log.e("定位方式： ", String.valueOf(aMapLocation.getLocationType() == 1 ? "GPS" : "网络"));

                Log.e("国家: ", String.valueOf(aMapLocation.getCountry()));
                Log.e("省份: ", String.valueOf(aMapLocation.getProvince()));
                Log.e("城市: ", String.valueOf(aMapLocation.getCity()));
                Log.e("区域: ", String.valueOf(aMapLocation.getDistrict()));
                Log.e("街道: ", String.valueOf(aMapLocation.getStreet()));
//                tvContext.setText(stringBuffer.toString());
                //显示比例尺控件

                //显示系统蓝点

            } else {
//            定位失败时可以通过ErrCode错误码）信息来确定失败的原因，ErrInfo是错误信息，详情见错误表
                Log.e("AmapError", "定位失败, ErrCode:"
                        + aMapLocation.getErrorCode() + ",errInfo:"
                        + aMapLocation.getErrorInfo());
            }


        }
    }


/*    @Override
    private void initView(){
//检索参数为城市和天气类型，实况天气为WEATHER_TYPE_LIVE、天气预报为WEATHER_TYPE_FORECAST
        mquery = new WeatherSearchQuery("北京", WeatherSearchQuery.WEATHER_TYPE_LIVE);
        mweathersearch=new WeatherSearch(this);
        mweathersearch.setOnWeatherSearchListener(this);
        mweathersearch.setQuery(mquery);
        mweathersearch.searchWeatherAsyn(); //异步搜索
    }*/

    /**
     * 请求权限结果     重写请求判断方法
     *
     * @param requestCode
     * @param permissions
     * @param grantResults
     */
    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        EasyPermissions.onRequestPermissionsResult(requestCode, permissions, grantResults, this);
    }

    /**
     * Toast提示
     *
     * @param msg 提示内容
     */
    private void showMsg(String msg) {
        Toast.makeText(this, msg, Toast.LENGTH_LONG).show();
    }

    @Override
    public void onPoiSearched(PoiResult poiResult, int i) {

    }

    @Override
    public void onPoiItemSearched(PoiItem poiItem, int i) {

    }


/**
 * 重写
 * 接受异步返回的定位结果
 */


    /**
     * 模拟位置是否启用
     * 若启用，则addTestProvider
     */
    /*public boolean getUseMockPosition(Context context) {
        // Android 6.0以下，通过Setting.Secure.ALLOW_MOCK_LOCATION判断
        // Android 6.0及以上，需要【选择模拟位置信息应用】，未找到方法，因此通过addTestProvider是否可用判断
        boolean canMockPosition = (Settings.Secure.getInt(context.getContentResolver(), Settings.Secure.ALLOW_MOCK_LOCATION, 0) != 0)
                || Build.VERSION.SDK_INT > 22;
        if (canMockPosition && hasAddTestProvider == false) {
            try {
                for (String providerStr : mockProviders) {
                    LocationProvider provider = mLocationManager.getProvider(providerStr);
                    if (provider != null) {
                        locationManager.addTestProvider(
                                provider.getName()
                                , provider.requiresNetwork()
                                , provider.requiresSatellite()
                                , provider.requiresCell()
                                , provider.hasMonetaryCost()
                                , provider.supportsAltitude()
                                , provider.supportsSpeed()
                                , provider.supportsBearing()
                                , provider.getPowerRequirement()
                                , provider.getAccuracy());
                    } else {
                        if (providerStr.equals(LocationManager.GPS_PROVIDER)) {
                            locationManager.addTestProvider(
                                    providerStr
                                    , true, true, false, false, true, true, true
                                    , Criteria.POWER_HIGH, Criteria.ACCURACY_FINE);
                        } else if (providerStr.equals(LocationManager.NETWORK_PROVIDER)) {
                            locationManager.addTestProvider(
                                    providerStr
                                    , true, false, true, false, false, false, false
                                    , Criteria.POWER_LOW, Criteria.ACCURACY_FINE);
                        } else {
                            locationManager.addTestProvider(
                                    providerStr
                                    , false, false, false, false, true, true, true
                                    , Criteria.POWER_LOW, Criteria.ACCURACY_FINE);
                        }
                    }
                    locationManager.setTestProviderEnabled(providerStr, true);
                    locationManager.setTestProviderStatus(providerStr, LocationProvider.AVAILABLE, null, System.currentTimeMillis());
                }
                hasAddTestProvider = true;  // 模拟位置可用
                canMockPosition = true;
            } catch (SecurityException e) {
                canMockPosition = false;
            }
        }
        if (canMockPosition == false) {
            stopMockLocation();
        }
        return canMockPosition;
    }*/
//www.biyezuopin.vip
//            接下来设置模拟经纬度数据

    // 模拟位置（addTestProvider成功的前提下）
/*
  for(String providerStr; mockProviders) {
        Location mockLocation = new Location(providerStr);
        mockLocation.setLatitude(latitude);   // 维度（度）
        mockLocation.setLongitude(longitude);  // 经度（度）
        mockLocation.setAccuracy(0.1f);   // 精度（米）
        mockLocation.setTime(new Date().getTime());   // 本地时间
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
            mockLocation.setElapsedRealtimeNanos(SystemClock.elapsedRealtimeNanos());
        }
        locationManager.setTestProviderLocation(providerStr, mockLocation);
    }
*/

    /* 取消位置模拟，以免启用模拟数据后无法还原使用系统位置
     * 若模拟位置未开启，则removeTestProvider将会抛出异常；
     * 若已addTestProvider后，关闭模拟位置，未removeTestProvider将导致系统GPS无数据更新；
     */
/*    public void stopMockLocation() {
        if (hasAddTestProvider) {
            for (String provider : mockProviders) {
                try {
                    locationManager.removeTestProvider(provider);
                } catch (Exception ex) {
                    // 此处不需要输出日志，若未成功addTestProvider，则必然会出错
                    // 这里是对于非正常情况的预防措施
                }
            }
            hasAddTestProvider = false;
        }
    }*/

}
