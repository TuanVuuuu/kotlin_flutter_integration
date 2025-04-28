import 'package:flutter/material.dart';
import 'package:get/get.dart';
import '../controllers/counter_controller.dart';
import '../controllers/platform_channel_controller.dart';

/// Blue square widget view
class BlueSquareView extends StatelessWidget {
  const BlueSquareView({super.key});

  @override
  Widget build(BuildContext context) {
    final CounterController controller = Get.find<CounterController>();
    final platformController = Get.find<PlatformChannelController>();
    
    return Scaffold(
      backgroundColor: Colors.transparent,
      body: Container(
        width: 200,
        height: 200,
        color: Colors.blue,
        child: Center(
          child: Column(
            mainAxisAlignment: MainAxisAlignment.center,
            children: [
              const Text(
                'Hình vuông xanh',
                style: TextStyle(
                  color: Colors.white,
                  fontSize: 18,
                  fontWeight: FontWeight.bold,
                ),
              ),
              const SizedBox(height: 10),
              Obx(() => Text(
                'Message: ${controller.message}',
                style: const TextStyle(color: Colors.white),
              )),
              const SizedBox(height: 15),
              ElevatedButton(
                onPressed: () => platformController.sendMessageToNative('Hello from Flutter Blue Square!'),
                style: ElevatedButton.styleFrom(
                  backgroundColor: Colors.white,
                  foregroundColor: Colors.blue,
                ),
                child: const Text('Send to Native'),
              ),
            ],
          ),
        ),
      ),
    );
  }
} 