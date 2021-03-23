import 'package:esptouch_smartconfig/esp_touch_result.dart';
import 'package:esptouch_smartconfig_example/connectivity_page.dart';
import 'package:flutter/material.dart';
import 'dart:async';
import 'package:esptouch_smartconfig/esptouch_smartconfig.dart';
import 'package:permission_handler/permission_handler.dart';

void main() async {
  WidgetsFlutterBinding.ensureInitialized();
  await Permission.location.request();
  runApp(MyApp());

}

class MyApp extends StatefulWidget {
  @override
  _MyAppState createState() => _MyAppState();
}

class _MyAppState extends State<MyApp> {
  String _platformVersion = 'Unknown';
  Future<Map<String,String>?>? a;
  List<ESPTouchResult?> list = [];
  @override
  void initState() {
    super.initState();
  }

  Future<Map<String, String>?>initPlatformState(){
    return EsptouchSmartconfig.wifiData();
  }
  // Platform messages are asynchronous, so we initialize in an async method.

  @override
  Widget build(BuildContext context) {
    return MaterialApp(home: ConnectivityPage());
    // return FutureBuilder(
    //   future: initPlatformState(),
    //     builder: (context, snapshot) {
    //
    //   if(snapshot.connectionState == ConnectionState.done) {
    //     print(snapshot.data);
    //     return MaterialApp(
    //           home: Scaffold(
    //             appBar: AppBar(
    //               title: const Text('Plugin example app'),
    //             ),
    //             body: StreamBuilder<ESPTouchResult?>(
    //               stream: EsptouchSmartconfig.run(),
    //               builder: (context, snapshot) {
    //                 if(snapshot.hasData){
    //                   list.add(snapshot.data);
    //                   return Container(
    //                     child: Text(snapshot.data!.bssid),
    //                   );
    //                 }
    //                 return Center(
    //                   child: Text('Running on: $_platformVersion\n'),
    //                 );
    //               }
    //             ),
    //           ),
    //         );
    //   }
    //   return Container();
    // });
  }
}
