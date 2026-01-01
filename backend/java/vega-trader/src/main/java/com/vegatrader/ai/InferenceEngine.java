package com.vegatrader.ai;

import java.util.List;
import java.util.Map;

/**
 * Interface for AI Inference Engine.
 * Abstraction for ONNX/TensorRT execution.
 */
public interface InferenceEngine {

    /**
     * Run inference on a named model.
     * 
     * @param modelName Name of the loaded model
     * @param inputs    Map of input tensor names to data
     * @return Map of output tensor names to data
     */
    Map<String, Object> runInference(String modelName, Map<String, Object> inputs);

    /**
     * Load a model from path.
     */
    void loadModel(String modelName, String modelPath);

    /**
     * Check if available.
     */
    boolean isAvailable();
}
