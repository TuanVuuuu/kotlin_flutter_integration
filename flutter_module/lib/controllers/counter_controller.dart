import 'package:get/get.dart';
import '../models/app_data_model.dart';

/// Main controller that handles the counter and message data
class CounterController extends GetxController {
  // Data model instance
  final AppDataModel model = AppDataModel();
  
  /// Get the current counter value
  int get count => model.counter.value;
  
  /// Get the current message
  String get message => model.message.value;
  
  /// Increment counter by 1
  void increment() {
    model.counter.value++;
  }
  
  /// Update message with a new value
  void updateMessage(String newMessage) {
    model.message.value = newMessage;
  }
  
  /// Reset counter to zero
  void resetCounter() {
    model.counter.value = 0;
  }
  
  /// Check if there's a message
  bool hasMessage() {
    return model.message.value.isNotEmpty;
  }
} 