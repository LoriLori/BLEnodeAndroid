var util = require('util');

var bleno = require('bleno');

var BlenoCharacteristic = bleno.Characteristic;

var EchoCharacteristic = function() {
  EchoCharacteristic.super_.call(this, {
    uuid: 'ec0e',
    properties: ['read', 'write', 'notify'],
    value: null
  });

  this._value = new Buffer(0);
  this._updateValueCallback = null;
};

util.inherits(EchoCharacteristic, BlenoCharacteristic);

EchoCharacteristic.prototype.onReadRequest = function(offset, callback) {
  console.log('EchoCharacteristic - onReadRequest: value = ' + this._value.toString('hex'));

  callback(this.RESULT_SUCCESS, this._value);
};

EchoCharacteristic.prototype.onWriteRequest = function(data, offset, withoutResponse, callback) {
  this._value = data;

  console.log('EchoCharacteristic - onWriteRequest: value = ' + this._value.toString('hex'));

  if (this._updateValueCallback) {
    console.log('EchoCharacteristic - onWriteRequest: notifying');

    //this._updateValueCallback(this._value);
	{
		var doc = {
		  "duration": 30, 
		  "rate": 0.0, 
		  "temp": "absolute"
		};

		// Serialize a document
		var data = new Buffer(JSON.stringify(doc),"ascii");
		console.log("data:", data, " len",data.length);

		for(i=0;i<data.length;i+=18) {
			var locBuff = new Buffer(2);locBuff.writeUIntLE(i/18,0,2);
			var sp = Buffer.concat([ locBuff  , data.slice(i,18+i)]);
			this._updateValueCallback(sp);
		}
		this._updateValueCallback(new Buffer(0));
	}
	
  }

  callback(this.RESULT_SUCCESS);
};

EchoCharacteristic.prototype.onSubscribe = function(maxValueSize, updateValueCallback) {
  console.log('EchoCharacteristic - onSubscribe');

  this._updateValueCallback = updateValueCallback;
};

EchoCharacteristic.prototype.onUnsubscribe = function() {
  console.log('EchoCharacteristic - onUnsubscribe');

  this._updateValueCallback = null;
};

module.exports = EchoCharacteristic;
