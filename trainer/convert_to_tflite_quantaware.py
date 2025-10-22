
import tensorflow as tf
import tensorflow_model_optimization as tfmot

# Load Keras model
model = tf.keras.models.load_model('player_model.h5', compile=False)

# Apply quantization aware training wrapper
quantize_model = tfmot.quantization.keras.quantize_model
q_model = quantize_model(model)
q_model.compile(optimizer='adam', loss={'action':'sparse_categorical_crossentropy','value':'mse'})

# You should fine-tune q_model on a small dataset here (omitted)
# Save then convert
q_model.save('player_model_q.h5')

converter = tf.lite.TFLiteConverter.from_keras_model(q_model)
converter.optimizations = [tf.lite.Optimize.DEFAULT]
converter.target_spec.supported_types = [tf.float16]
tflite_model = converter.convert()
open('player_model.tflite','wb').write(tflite_model)
print('Converted to TFLite, size KB:', len(tflite_model)/1024)
