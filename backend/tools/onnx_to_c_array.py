#Converts exported ONNX model to MCU-Ready Weights
import subprocess
import onnx
from onnx_tf.backend import prepare
import tensorflow as tf
from tensorflow.lite.python.util import convert_bytes_to_c_source

#1. Convert ONNX model to tflite
def conversion():
    print("1")
    onnx_model = onnx.load("backend/model/audio_classifier_model.onnx")
    tf_rep = prepare(onnx_model)
    tf_rep.export_graph("audio_classifier_model_tf")
    print("2")
    #tf to tflite
    converter = tf.lite.TFLiteConverter.from_saved_model("audio_classifier_model_tf")

    #2. Quantize it to int8
    converter.optimizations = [tf.lite.Optimize.DEFAULT]
    converter.target_spec.supported_ops = [tf.lite.OpsSet.TFLITE_BUILTINS_INT8]


    tflite_model = converter.convert()
    print("3")
    with open("backed/model/audio_classifier_model.tflite", 'wb') as f:
        f.write(tflite_model)
    




#3. Embed it as C array for MCU server
def embedding():
    with open("backed/model/audio_classifier_model.tflite", 'rb') as f:
        tflite_model = f.read()

    source_text, header_text = convert_bytes_to_c_source(tflite_model, "audio_classifier_model_data")

    with open("model_data.h", "w") as f:
        f.write(header_text)
    with open("model_data.cc", "w") as f:
        f.write(source_text)


if __name__ == "__main__":
    conversion()
    embedding()