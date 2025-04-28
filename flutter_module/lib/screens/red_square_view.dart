import 'package:flutter/material.dart';
import 'package:get/get.dart';
import '../controllers/counter_controller.dart';

/// Red square widget view
class RedSquareView extends StatelessWidget {
  const RedSquareView({super.key});

  @override
  Widget build(BuildContext context) {
    final CounterController controller = Get.find<CounterController>();
    
    return Scaffold(
      backgroundColor: Colors.transparent,
      body: Container(
        width: 200,
        height: 200,
        color: Colors.red,
        child: Center(
          child: Column(
            mainAxisAlignment: MainAxisAlignment.center,
            children: [
              const Text(
                'Hình vuông đỏ',
                style: TextStyle(
                  color: Colors.white,
                  fontSize: 18,
                  fontWeight: FontWeight.bold,
                ),
              ),
              const SizedBox(height: 10),
              Obx(() => Text(
                'Counter: ${controller.count}',
                style: const TextStyle(color: Colors.white),
              )),
            ],
          ),
        ),
      ),
    );
  }
} 