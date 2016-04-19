package com.eric.baidumap;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import com.baidu.location.BDLocation;
import com.baidu.location.BDLocationListener;
import com.baidu.location.LocationClient;
import com.baidu.location.LocationClientOption;
import com.baidu.location.Poi;
import com.baidu.mapapi.SDKInitializer;
import com.baidu.mapapi.map.BaiduMap;
import com.baidu.mapapi.map.BitmapDescriptor;
import com.baidu.mapapi.map.BitmapDescriptorFactory;
import com.baidu.mapapi.map.MapView;
import com.baidu.mapapi.map.MarkerOptions;
import com.baidu.mapapi.map.OverlayOptions;
import com.baidu.mapapi.model.LatLng;
import com.baidu.mapapi.search.core.PoiInfo;
import com.baidu.mapapi.search.core.SearchResult;
import com.baidu.mapapi.search.poi.OnGetPoiSearchResultListener;
import com.baidu.mapapi.search.poi.PoiCitySearchOption;
import com.baidu.mapapi.search.poi.PoiDetailResult;
import com.baidu.mapapi.search.poi.PoiResult;
import com.baidu.mapapi.search.poi.PoiSearch;

import java.util.List;

public class MainActivity extends AppCompatActivity {

    private MapView mMapView = null;
    private BaiduMap baiduMap;
    private PoiSearch mPoiSearch;
    public LocationClient mLocationClient = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //在使用SDK各组件之前初始化context信息，传入ApplicationContext
        //注意该方法要再setContentView方法之前实现
        SDKInitializer.initialize(getApplicationContext());
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        toolbar.setNavigationIcon(android.R.drawable.ic_menu_edit);
        toolbar.setLogo(android.R.drawable.star_big_on);
        toolbar.setTitle("线路跟踪案例");
        setSupportActionBar(toolbar);

        mMapView = (MapView) findViewById(R.id.bmapView);
        baiduMap = mMapView.getMap();//获取地图
//        baiduMap.setMapType(BaiduMap.MAP_TYPE_SATELLITE);//显示卫星图
        baiduMap.setTrafficEnabled(true);//实时交通图

        BDLocationListener myListener = new MyLocationListener();
        mLocationClient = new LocationClient(getApplicationContext());  //声明LocationClient类
        mLocationClient.registerLocationListener( myListener );    //注册监听函数

        //添加一个标注覆盖物
        LatLng point = new LatLng(39.963175, 116.400244);
        final BitmapDescriptor bitmap = BitmapDescriptorFactory.fromResource(R.drawable.marker);
        //构建MarkerOption，用于在地图上添加Marker
        OverlayOptions option = new MarkerOptions().position(point).icon(bitmap);
        //在地图上添加Marker，并显示
        baiduMap.addOverlay(option);

        //创建POI检索实例
        mPoiSearch = PoiSearch.newInstance();
        //创建POI检索监听者；
        OnGetPoiSearchResultListener poiListener = new OnGetPoiSearchResultListener() {
            @Override
            public void onGetPoiResult(PoiResult result) {
                //获取POI检索结果
                if (result == null || result.error != SearchResult.ERRORNO.NO_ERROR){
                    return;
                }
                List<PoiInfo> poiInfos = result.getAllPoi();
                //遍历所有POI
                for (PoiInfo p: poiInfos){
                    BitmapDescriptor bitmap = BitmapDescriptorFactory.fromResource(R.drawable.marker);
                    OverlayOptions option = new MarkerOptions().position(p.location).icon(bitmap);
                    baiduMap.addOverlay(option);
                }
            }

            @Override
            public void onGetPoiDetailResult(PoiDetailResult poiDetailResult) {
                //获取Place详情页检索结果
            }
        };
        //设置POI检索监听者；
        mPoiSearch.setOnGetPoiSearchResultListener(poiListener);
//        mPoiSearch.destroy();

    }

    //搜索结果
    public void SearchResult(View v){
        //发起检索请求；异步过程
        mPoiSearch.searchInCity((new PoiCitySearchOption())
                .city("北京")
                .keyword("美食")
                .pageNum(20));
    }

    //定位我的位置信息
    public void MyLocation(View v){
        initLocation();
    }

    //配置配置定位SDK参数
    private void initLocation(){
        LocationClientOption option = new LocationClientOption();
        option.setLocationMode(LocationClientOption.LocationMode.Hight_Accuracy
        );//可选，默认高精度，设置定位模式，高精度，低功耗，仅设备
        option.setCoorType("bd09ll");//可选，默认gcj02，设置返回的定位结果坐标系
        int span=5000;
        option.setScanSpan(span);//可选，默认0，即仅定位一次，设置发起定位请求的间隔需要大于等于1000ms才是有效的
        option.setIsNeedAddress(true);//可选，设置是否需要地址信息，默认不需要
        option.setOpenGps(true);//可选，默认false,设置是否使用gps
        option.setLocationNotify(true);//可选，默认false，设置是否当gps有效时按照1S1次频率输出GPS结果
        option.setIsNeedLocationDescribe(true);//可选，默认false，设置是否需要位置语义化结果，可以在BDLocation.getLocationDescribe里得到，结果类似于“在北京天安门附近”
        option.setIsNeedLocationPoiList(true);//可选，默认false，设置是否需要POI结果，可以在BDLocation.getPoiList里得到
        option.setIgnoreKillProcess(false);//可选，默认true，定位SDK内部是一个SERVICE，并放到了独立进程，设置是否在stop的时候杀死这个进程，默认不杀死
        option.SetIgnoreCacheException(false);//可选，默认false，设置是否收集CRASH信息，默认收集
        option.setEnableSimulateGps(false);//可选，默认false，设置是否需要过滤gps仿真结果，默认需要
        mLocationClient.setLocOption(option);
        mLocationClient.start();
        mLocationClient.requestLocation();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mMapView.onDestroy();
    }
    @Override
    protected void onResume() {
        super.onResume();
        mMapView.onResume();
    }
    @Override
    protected void onPause() {
        super.onPause();
        mMapView.onPause();
    }

    //位置监听器
    private class MyLocationListener implements BDLocationListener {
        @Override
        public void onReceiveLocation(BDLocation location) { //接收位置信息的回调方法
            StringBuffer sb = new StringBuffer(256);
            sb.append("time : ");
            sb.append(location.getTime());
            sb.append("\nerror code : ");
            sb.append(location.getLocType());
            sb.append("\nlatitude : ");
            sb.append(location.getLatitude());
            sb.append("\nlontitude : ");
            sb.append(location.getLongitude());
            sb.append("\nradius : ");
            sb.append(location.getRadius());
            if (location.getLocType() == BDLocation.TypeGpsLocation){// GPS定位结果
                sb.append("\nspeed : ");
                sb.append(location.getSpeed());// 单位：公里每小时
                sb.append("\nsatellite : ");
                sb.append(location.getSatelliteNumber());
                sb.append("\nheight : ");
                sb.append(location.getAltitude());// 单位：米
                sb.append("\ndirection : ");
                sb.append(location.getDirection());// 单位度
                sb.append("\naddr : ");
                sb.append(location.getAddrStr());
                sb.append("\ndescribe : ");
                sb.append("gps定位成功");

            } else if (location.getLocType() == BDLocation.TypeNetWorkLocation){// 网络定位结果
                sb.append("\naddr : ");
                sb.append(location.getAddrStr());
                //运营商信息
                sb.append("\noperationers : ");
                sb.append(location.getOperators());
                sb.append("\ndescribe : ");
                sb.append("网络定位成功");
            } else if (location.getLocType() == BDLocation.TypeOffLineLocation) {// 离线定位结果
                sb.append("\ndescribe : ");
                sb.append("离线定位成功，离线定位结果也是有效的");
            } else if (location.getLocType() == BDLocation.TypeServerError) {
                sb.append("\ndescribe : ");
                sb.append("服务端网络定位失败，可以反馈IMEI号和大体定位时间到loc-bugs@baidu.com，会有人追查原因");
            } else if (location.getLocType() == BDLocation.TypeNetWorkException) {
                sb.append("\ndescribe : ");
                sb.append("网络不同导致定位失败，请检查网络是否通畅");
            } else if (location.getLocType() == BDLocation.TypeCriteriaException) {
                sb.append("\ndescribe : ");
                sb.append("无法获取有效定位依据导致定位失败，一般是由于手机的原因，处于飞行模式下一般会造成这种结果，可以试着重启手机");
            }
            sb.append("\nlocationdescribe : ");
            sb.append(location.getLocationDescribe());// 位置语义化信息
            List<Poi> list = location.getPoiList();// POI数据
            if (list != null) {
                sb.append("\npoilist size = : ");
                sb.append(list.size());
                for (Poi p : list) {
                    sb.append("\npoi= : ");
                    sb.append(p.getId() + " " + p.getName() + " " + p.getRank());
                }
            }

            System.out.println(sb.toString());
            BitmapDescriptor bitmap = BitmapDescriptorFactory.fromResource(R.drawable.marker);
            LatLng point = new LatLng(location.getLatitude(),location.getLongitude());
            OverlayOptions option = new MarkerOptions().position(point).icon(bitmap);
            baiduMap.addOverlay(option);

        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main,menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.my_place){
            return true;
        }else if (id == R.id.srt_trace){

        }
        else if (id == R.id.end_trace){

        }
        else if (id == R.id.trace_back){

        }

        return super.onOptionsItemSelected(item);
    }
}
