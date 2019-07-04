


String read_data_from_camera(){
  if(Serial.available()){
    serial_data = Serial.readString();
  }
  return serial_data;
}
