import 'package:get/get.dart';

/// Base data model for counter and messages
class AppDataModel {
  final RxInt counter;
  final RxString message;
  
  AppDataModel({int initialCount = 0, String initialMessage = ''})
      : counter = initialCount.obs,
        message = initialMessage.obs;
} 