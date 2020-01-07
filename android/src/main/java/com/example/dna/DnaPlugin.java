package com.example.dna;

import com.example.dna.model.DnaClassInfo;
import com.example.dna.model.ParameterInfo;
import com.example.dna.util.GsonUtils;

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

    public static final String EXECUTENATIVECONTEXT = "executeNativeContext";
    public static final String INVOCATIONNODES = "_invocationNodes";
    public static final String OBJECTJSONWRAPPERS = "_objectJSONWrappers";
    public static final String RETURNVAR = "returnVar";
    public static final String OBJECTID = "_objectId";
    public static final String DNA_JSON = "json";
    public static final String DNA_CLS = "cls";
    public static final String DNA_OBJECT = "object";
    public static final String DNA_CLS_NAME = "clsName";
    public static final String DNA_METHOD = "method";
    public static final String DNA_ARGS = "args";
    public static final String DNA_CONTRUCT_ARGS = "contructArgs";
    public static final String DNA_CONTRUCT_CLS = "contructCls";

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
        } else if (call.method.equals(EXECUTENATIVECONTEXT)) {
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
        List<Map<String, Object>> invocationNodes = call.argument(INVOCATIONNODES);
        List<Map<String, Object>> objectJSONs = call.argument(OBJECTJSONWRAPPERS);
        Map<String, Object> valueMap = new HashMap<>();// 用于映射id和返回值
        if (invocationNodes == null || invocationNodes.isEmpty()) {
            return;
        }
        Map<String, String> returnVar = call.argument(RETURNVAR);
        String finalReturnVarId = getReturnString(returnVar);

        for (Map<String, Object> ojectJson : objectJSONs) {
            String objectId = String.valueOf(ojectJson.get(OBJECTID));
            String content = GsonUtils.toJson(ojectJson.get(DNA_JSON));
            String classType = String.valueOf(ojectJson.get(DNA_CLS));
            valueMap.put(objectId, new ParameterInfo(content, classType));
        }

        Map<String, Object> idMap;
        String clsName;
        String constructName;
        String nodeId;
        String methodName;
        List<Object> argsMap;

        List<ParameterInfo> parameterInfos = new ArrayList<>();
        Object currentObject = null;
        for (Map<String, Object> node : invocationNodes) {
            idMap = (Map<String, Object>) node.get(DNA_OBJECT);
            nodeId = (String) idMap.get(OBJECTID);
            if (idMap.containsKey(DNA_CLS_NAME)) {
                clsName = (String) idMap.get(DNA_CLS_NAME);
                valueMap.put(nodeId, new DnaClassInfo(clsName));
            } else if (!valueMap.containsKey(nodeId) && idMap.containsKey(DNA_CONTRUCT_CLS)) {
                constructName = (String) idMap.get(DNA_CONTRUCT_CLS);
                valueMap.put(nodeId, DnaClient.getClient().invokeConstructorMethod(constructName, getParameters(valueMap, (List<Object>) idMap.get(DNA_CONTRUCT_ARGS))));
                continue;
            }
            parameterInfos.clear();

            methodName = String.valueOf(node.get(DNA_METHOD));
            argsMap = (List<Object>) node.get(DNA_ARGS);
            parameterInfos = getParameters(valueMap, argsMap);

            String returnId = getReturnString((Map<String, String>) node.get(RETURNVAR));
            Object ownerObject = valueMap.get(nodeId);
            if (ownerObject == null) {
                return;
            } else if (ownerObject instanceof DnaClassInfo) {
                currentObject = DnaClient.getClient().invokeMethod(((DnaClassInfo) ownerObject).getClassName(), null, methodName, parameterInfos);
            } else {
                currentObject = DnaClient.getClient().invokeMethod(ownerObject.getClass().getName(), ownerObject, methodName, parameterInfos);
            }
            valueMap.put(returnId, currentObject);
            if (returnId != null && returnId.equals(finalReturnVarId)) {
                result.success(valueMap.get(returnId));
            }
        }
        result.success(currentObject);
    }


    private String getReturnString(Map<String, String> returnVar) {
        String returnVarId = null;
        if (returnVar != null && !returnVar.isEmpty()) {
            returnVarId = returnVar.get(OBJECTID);
        }
        return returnVarId;
    }

    private List<ParameterInfo> getParameters(Map<String, Object> valueMap, List<Object> argsMap) {
        String tempId;
        Object tempContent;
        List<ParameterInfo> parameters = new ArrayList<>();
        if (argsMap != null && !argsMap.isEmpty()) {
            for (Object o : argsMap) {
                if (o instanceof Map) {
                    tempId = String.valueOf(((Map) o).get(OBJECTID));
                    tempContent = valueMap.get(tempId);
                    if (tempContent instanceof ParameterInfo) {
                        parameters.add((ParameterInfo) tempContent);
                    } else {
                        parameters.add(new ParameterInfo(GsonUtils.toJson(tempContent), tempContent.getClass().getName()));
                    }
                } else {
                    parameters.add(new ParameterInfo(String.valueOf(o)));

                }
            }
        }
        return parameters;
    }


}
