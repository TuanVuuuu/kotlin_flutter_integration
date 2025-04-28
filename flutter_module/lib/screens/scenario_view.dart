import 'package:flutter_module/controllers/scenario_controller.dart';
import 'package:get/get.dart';
import 'package:flutter/material.dart';
import 'package:flutter/services.dart';

class ScenarioView extends GetView<ScenarioController> {
  ScenarioView({Key? key}) : super(key: key);

  final _scaffoldKey = GlobalKey<ScaffoldState>();
  final _methodChannel = const MethodChannel('flutter_module_channel');

  @override
  Widget build(BuildContext context) {
    _setupMethodChannel();
    
    return Scaffold(
      key: _scaffoldKey,
      appBar: AppBar(
        title: const Text('Scenario View'),
        automaticallyImplyLeading: false,
      ),
      body: SingleChildScrollView(
        child: Center(
          child: Column(
            mainAxisAlignment: MainAxisAlignment.center,
            children: [
              ElevatedButton(
                onPressed: () {
                  debugPrint('Button 1 pressed');
                  if (_scaffoldKey.currentContext != null) {
                    debugPrint('Showing Snackbar for Button 1');
                    ScaffoldMessenger.of(_scaffoldKey.currentContext!).showSnackBar(
                      const SnackBar(
                        content: Text('Button 1 pressed'),
                        duration: Duration(seconds: 2),
                      ),
                    );
                  } else {
                    debugPrint('Error: Scaffold context is null');
                  }
                },
                child: const Text('Button 1'),
              ),
              const SizedBox(height: 20),
              ElevatedButton(
                onPressed: () {
                  debugPrint('Button 2 pressed');
                  if (_scaffoldKey.currentContext != null) {
                    debugPrint('Showing Snackbar for Button 2');
                    ScaffoldMessenger.of(_scaffoldKey.currentContext!).showSnackBar(
                      const SnackBar(
                        content: Text('Button 2 pressed'),
                        duration: Duration(seconds: 2),
                      ),
                    );
                  } else {
                    debugPrint('Error: Scaffold context is null');
                  }
                },
                child: const Text('Button 2'),
              ),
              ListView.builder(
                shrinkWrap: true,
                physics: const NeverScrollableScrollPhysics(),
                itemCount: 20,
                itemBuilder: (context, index) {
                  return ListTile(
                    title: Text('Item $index'),
                    onTap: () {
                      debugPrint('List item $index tapped');
                      if (_scaffoldKey.currentContext != null) {
                        debugPrint('Showing Snackbar for item $index');
                        ScaffoldMessenger.of(_scaffoldKey.currentContext!).showSnackBar(
                          SnackBar(
                            content: Text('Item $index tapped'),
                            duration: const Duration(seconds: 2),
                          ),
                        );
                      }
                    },
                  );
                },
              ),
            ],
          ),
        ),
      ),
    );
  }

  void _setupMethodChannel() {
    _methodChannel.setMethodCallHandler((call) async {
      if (call.method == 'showMessage') {
        final message = call.arguments as String;
        debugPrint('Received message from Android: $message');
        if (_scaffoldKey.currentContext != null) {
          ScaffoldMessenger.of(_scaffoldKey.currentContext!).showSnackBar(
            SnackBar(content: Text(message)),
          );
        }
      }
    });
  }
}
