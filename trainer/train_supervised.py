
import tensorflow as tf
from tensorflow import keras
from tensorflow.keras import layers
import numpy as np

# MobileNetV3-like small head (not exact but lightweight)
def build_model():
    inputs = layers.Input(shape=(128,128,12))
    x = layers.Conv2D(16,3,activation='relu',padding='same')(inputs)
    x = layers.MaxPooling2D(2)(x)
    x = layers.Conv2D(32,3,activation='relu',padding='same')(x)
    x = layers.MaxPooling2D(2)(x)
    x = layers.SeparableConv2D(64,3,activation='relu',padding='same')(x)
    x = layers.GlobalAveragePooling2D()(x)
    x = layers.Dense(256,activation='relu')(x)
    x = layers.Dropout(0.3)(x)
    x = layers.Dense(128,activation='relu')(x)
    action_out = layers.Dense(10,activation='softmax',name='action')(x)
    value_out = layers.Dense(1,name='value')(x)
    model = keras.Model(inputs, [action_out, value_out])
    model.compile(optimizer=keras.optimizers.Adam(0.0005),
                  loss={'action':'sparse_categorical_crossentropy','value':'mse'},
                  loss_weights={'action':1.0,'value':0.1},
                  metrics={'action':'accuracy'})
    return model

# augmentation helper
def augment(x):
    # simple random flips and brightness jitter
    if np.random.rand() < 0.5:
        x = np.flip(x, axis=2)
    x = x * (0.8 + 0.4 * np.random.rand())
    return np.clip(x, 0, 1)

if __name__ == '__main__':
    # placeholder data generator; replace with your dataset
    X = np.random.rand(200,128,128,12).astype('float32')
    y = np.random.randint(0,10,size=(200,))
    model = build_model()
    model.fit(X, {'action':y, 'value': np.zeros(len(y))}, epochs=10, batch_size=16, validation_split=0.1,
              callbacks=[keras.callbacks.EarlyStopping(patience=3, restore_best_weights=True)])
    model.save('player_model.h5')
