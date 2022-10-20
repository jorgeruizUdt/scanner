import 'package:flutter/material.dart';
import 'package:flutter/services.dart';

void main() {
  runApp(const MyApp());
}

class MyApp extends StatelessWidget {
  const MyApp({super.key});

  // This widget is the root of your application.
  @override
  Widget build(BuildContext context) {
    return MaterialApp(
      title: 'Flutter Scanner app',
      theme: ThemeData(
        brightness: Brightness.dark,
        primaryColor: Colors.lightBlue[800],
      ),
      home: const MyHomePage(title: 'ScannerApp'),
    );
  }
}

class MyHomePage extends StatefulWidget {
  const MyHomePage({super.key, required this.title});

  final String title;

  @override
  State<MyHomePage> createState() => _MyHomePageState();
}

class _MyHomePageState extends State<MyHomePage> {
  static const scannerChannel = MethodChannel("example.flutter.dev/scanner");
  String? _scannerResult;

  Future<void> openScanner() async {
    String result;

    try {
      final String channelResult = await scannerChannel.invokeMethod('getCode');
      result = channelResult;
    } on PlatformException catch (error) {
      result = "Platform error \n ${error.message}";
    }

    if (!mounted) return;

    setState(() {
      _scannerResult = result;
    });
  }


  @override
  Widget build(BuildContext context) {
    return Scaffold(
      appBar: AppBar(
        title: Text(widget.title),
      ),
      body: Center(
        child: Column(
          mainAxisAlignment: MainAxisAlignment.center,
          children: <Widget>[
            ElevatedButton.icon(
              style: ElevatedButton.styleFrom(
                foregroundColor: Colors.black, 
                backgroundColor: Colors.amber
              ),
              icon: const Icon(Icons.camera),
              label: const Text('Scan'),
              onPressed: openScanner,
            ),
            const SizedBox(height: 20),
            Text(
              _scannerResult == null
              ? ''
              : 'Scan result: $_scannerResult',
              style: const TextStyle(fontSize: 18),
            ),
          ],
        ),
      ),
    );
  }
}
