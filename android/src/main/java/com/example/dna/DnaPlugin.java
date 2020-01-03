package com.example.dna;

import com.example.dna.model.DnaClassNode;
import com.example.dna.model.DnaNode;
import com.example.dna.model.DnaResult;
import com.example.dna.model.ParameterInfo;
import com.example.dna.util.GsonUtils;

import org.w3c.dom.Node;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import io.flutter.plugin.common.MethodCall;
import io.flutter.plugin.common.MethodChannel;
import io.flutter.plugin.common.MethodChannel.MethodCallHandler;
import io.flutter.plugin.common.MethodChannel.Result;
import io.flutter.plugin.common.PluginRegistry.Registrar;

/**
 * DnaPlugin
 */
public class DnaPlugin implements MethodCallHandler {
    /**
     * Plugin registration.
     */
    public static void registerWith(Registrar registrar) {
        final MethodChannel channel = new MethodChannel(registrar.messenger(), "dna");
        channel.setMethodCallHandler(new DnaPlugin());
    }

    @Override
    public void onMethodCall(MethodCall call, Result result) {
        if (call.method.equals("getPlatformVersion")) {
            result.success("Android " + android.os.Build.VERSION.RELEASE);
        } else if (call.method.equals("executeNativeContext")) {
            try {
                excuteNativeMethod(call, result);
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else {
            result.notImplemented();
        }
    }

    private void excuteNativeMethod(MethodCall call, Result result) throws Exception {
        List<Map<String, Object>> invocationNodes = call.argument("_invocationNodes");
        List<Map<String, Object>> objectJSONs = call.argument("_objectJSONWrappers");
        Map<String, Object> valueMap = new HashMap<>();// 用于映射id和返回值
        List<DnaClassNode> dnaClassNodeList = new ArrayList<>();
        if (invocationNodes == null || invocationNodes.isEmpty()) {
            return;
        }
        Map<String, String> returnVar = call.argument("returnVar");
        String finalReturnVarId = getReturnString(returnVar);

        for (Map<String, Object> ojectJson : objectJSONs) {
            String objectId = String.valueOf(ojectJson.get("_objectId"));
            String content = GsonUtils.toJson(ojectJson.get("json"));
            String classType = String.valueOf(ojectJson.get("cls"));
            valueMap.put(objectId, new ParameterInfo(content, classType));
        }

        Map<String, String> idMap;
        String clsName;
        String nodeId;
        String methodName;
        List<Object> argsMap;
        String tempId;
        Object tempContent;
        DnaClassNode currentClass;
        List<ParameterInfo> parameterInfos = new ArrayList<>();
        Object currentObject = null;
        for (Map<String, Object> node : invocationNodes) {
            currentObject = null;
            idMap = (Map<String, String>) node.get("object");
            nodeId = idMap.get("_objectId");
            if (idMap.containsKey("clsName")) {
                clsName = idMap.get("clsName");
                dnaClassNodeList.add(new DnaClassNode(nodeId, clsName));
                valueMap.put(nodeId, DnaClient.getClient().invokeConstructorMethod(clsName, null));
            }
            currentClass = null;
            parameterInfos.clear();
            for (DnaClassNode classNode : dnaClassNodeList) {
                if (classNode.hasMethod(nodeId) || (nodeId != null && nodeId.equals(classNode.getClassNodeId()))) {
                    classNode.addMethodId(getReturnString((Map<String, String>) node.get("returnVar")));
                    currentClass = classNode;
                    break;
                }
            }

            methodName = String.valueOf(node.get("method"));
            argsMap = (List<Object>) node.get("args");
            if (argsMap != null && !argsMap.isEmpty()) {
                for (Object o : argsMap) {
                    if (o instanceof Map) {
                        tempId = String.valueOf(((Map) o).get("_objectId"));
                        tempContent = valueMap.get(tempId);
                        if (tempContent instanceof ParameterInfo) {
                            parameterInfos.add((ParameterInfo) tempContent);
                        } else {
                            parameterInfos.add(new ParameterInfo(GsonUtils.toJson(tempContent), tempContent.getClass().getName()));
                        }
                    } else {
                        parameterInfos.add(new ParameterInfo(String.valueOf(o)));

                    }
                }
            }
            String returnId = getReturnString((Map<String, String>) node.get("returnVar"));
            if (currentClass == null) {
                return;
            } else {
                currentObject = DnaClient.getClient().invokeMethod(currentClass.getClassName(), valueMap.get(nodeId), methodName, parameterInfos);
                valueMap.put(returnId, currentObject);
            }
            if (returnId != null && returnId.equals(finalReturnVarId)) {
                result.success(valueMap.get(returnId));
            }
        }
        result.success(currentObject);
    }


    private String getReturnString(Map<String, String> returnVar) {
        String returnVarId = null;
        if (returnVar != null && !returnVar.isEmpty()) {
            returnVarId = returnVar.get("_objectId");
        }
        return returnVarId;
    }


}
