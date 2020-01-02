import 'package:flutter/services.dart';
import 'package:flutter_test/flutter_test.dart';
import 'package:dna/dna.dart';

void main() {
  const MethodChannel channel = MethodChannel('dna');

  setUp(() {
    channel.setMockMethodCallHandler((MethodCall methodCall) async {
      return '42';
    });
  });

  tearDown(() {
    channel.setMockMethodCallHandler(null);
  });

  test('getPlatformVersion', () async {
    
  });
}
