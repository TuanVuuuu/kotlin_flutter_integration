import 'package:flutter/material.dart';
import 'package:flutter_module/screens/scenario_view.dart';
import 'package:get/get.dart';

// Import constants
import 'constants.dart';

// Import controllers
import 'controllers/counter_controller.dart';
import 'controllers/platform_channel_controller.dart';

void main() {
  WidgetsFlutterBinding.ensureInitialized();
  runApp(const MyApp());
}

/// Main App Widget
class MyApp extends StatelessWidget {
  const MyApp({super.key});

  @override
  Widget build(BuildContext context) {
    // Initialize controllers
    _initializeControllers();

    return GetMaterialApp(
      debugShowCheckedModeBanner: false,
      title: 'Flutter Module',
      theme: _buildAppTheme(),
      initialRoute: '/',
      getPages: _createAppRoutes(),
    );
  }

  /// Initialize all required controllers
  void _initializeControllers() {
    // Register controllers with GetX dependency injection
    Get.put(CounterController());
    Get.put(PlatformChannelController());
  }

  /// Build app theme
  ThemeData _buildAppTheme() {
    return ThemeData(
      primarySwatch: Colors.blue,
      visualDensity: VisualDensity.adaptivePlatformDensity,
    );
  }

  /// Create application routes
  List<GetPage> _createAppRoutes() {
    return [
      GetPage(name: '/', page: () => const HomePage()),
      GetPage(name: '/$RED_SQUARE_ROUTE', page: () => const RedSquareView()),
      GetPage(name: '/$BLUE_SQUARE_ROUTE', page: () => const BlueSquareView()),
      GetPage(name: '/$SCENARIO_VIEW', page: () => ScenarioView()),
    ];
  }
}

/// Home page view
class HomePage extends StatelessWidget {
  const HomePage({super.key});

  @override
  Widget build(BuildContext context) {
    final CounterController controller = Get.find<CounterController>();

    return Scaffold(
      appBar: AppBar(
        title: const Text('Flutter Module Home'),
      ),
      body: Center(
        child: Column(
          mainAxisAlignment: MainAxisAlignment.center,
          children: <Widget>[
            const Text(
              'Đây là màn hình Flutter mặc định',
              style: TextStyle(fontSize: 16),
            ),
            const SizedBox(height: 20),
            _buildCounterDisplay(controller),
            _buildMessageDisplay(controller),
          ],
        ),
      ),
      floatingActionButton: FloatingActionButton(
        onPressed: controller.increment,
        tooltip: 'Increment',
        child: const Icon(Icons.add),
      ),
    );
  }

  /// Build the counter display widget
  Widget _buildCounterDisplay(CounterController controller) {
    return Obx(() => Text(
          'Counter: ${controller.count}',
          style: const TextStyle(fontSize: 24, fontWeight: FontWeight.bold),
        ));
  }

  /// Build the message display widget
  Widget _buildMessageDisplay(CounterController controller) {
    return Obx(() => Visibility(
          visible: controller.hasMessage(),
          child: Padding(
            padding: const EdgeInsets.all(16.0),
            child: Text(
              'Message từ Native: ${controller.message}',
              style: const TextStyle(
                color: Colors.blue,
                fontWeight: FontWeight.bold,
                fontSize: 16,
              ),
            ),
          ),
        ));
  }
}

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
                onPressed: () => platformController
                    .sendMessageToNative('Hello from Flutter Blue Square!'),
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
