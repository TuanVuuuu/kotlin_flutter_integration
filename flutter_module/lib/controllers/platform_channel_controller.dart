import 'package:flutter/services.dart';
import 'package:get/get.dart';
import '../constants.dart';
import 'counter_controller.dart';

/// Controller for handling communication with native platform
class PlatformChannelController extends GetxController {
  // Platform channel instance
  static const MethodChannel _platform = MethodChannel(CHANNEL_NAME);
  
  // Reference to the counter controller
  late final CounterController _counterController;
  
  @override
  void onInit() {
    super.onInit();
    // Get the CounterController instance
    _counterController = Get.find<CounterController>();
    // Set up method channel handler
    _setupMethodCallHandler();
  }
  
  /// Configure the method channel to handle incoming calls
  void _setupMethodCallHandler() {
    _platform.setMethodCallHandler(_handleMethodCall);
  }
  
  /// Process method calls from the native platform
  Future<dynamic> _handleMethodCall(MethodCall call) async {
    switch (call.method) {
      case 'showMessage':
        return _handleShowMessage(call.arguments);
      case 'resetCounter':
        return _handleResetCounter();
      default:
        return _handleUnknownMethod(call.method);
    }
  }
  
  /// Handle showMessage call from native
  Future<String> _handleShowMessage(dynamic arguments) async {
    String message = arguments ?? '';
    _counterController.updateMessage(message);
    return 'Message displayed: $message';
  }
  
  /// Handle resetCounter call from native
  Future<String> _handleResetCounter() async {
    _counterController.resetCounter();
    return 'Counter reset to 0';
  }
  
  /// Handle unknown method calls
  Future<String> _handleUnknownMethod(String method) async {
    return 'Method not implemented: $method';
  }
  
  /// Send a message to the native platform
  Future<void> sendMessageToNative(String message) async {
    try {
      await _platform.invokeMethod('messageFromFlutter', message);
    } catch (e) {
      print('Error sending message to native: $e');
    }
  }
} 