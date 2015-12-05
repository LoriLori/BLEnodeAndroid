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
  this._pos = 0;
  this._doc = null;
};

var Transfer = {
    _doc : null,
    _pos : 0,
    _updateValueCallback : null,
    _start : true,
    
    transfer : function() {
        if((Transfer._pos<Transfer._doc.length && Transfer._pos!=0) || Transfer._start === true) {
            var locBuff = new Buffer(2);locBuff.writeUIntLE(Transfer._pos/10,0,2);
            var sp = Buffer.concat([ locBuff  , Transfer._doc.slice(Transfer._pos,10+Transfer._pos)]);
            console.log('pos: ',Transfer._pos, ' ', sp);
            Transfer._pos += 10;
            Transfer._start = false;
            Transfer._updateValueCallback(sp);
        } else if(Transfer._pos!=0){
            Transfer._pos = 0;
            console.log('pos: ',Transfer._pos);
            Transfer._updateValueCallback(new Buffer(0));

        }
  

        
    }
};

util.inherits(EchoCharacteristic, BlenoCharacteristic);

EchoCharacteristic.prototype.onReadRequest = function(offset, callback) {
  console.log('EchoCharacteristic - onReadRequest');

        if((Transfer._pos<Transfer._doc.length && Transfer._pos!=0) || Transfer._start === true) {
            var locBuff = new Buffer(2);locBuff.writeUIntLE(Transfer._pos/10,0,2);
            var sp = Buffer.concat([ locBuff  , Transfer._doc.slice(Transfer._pos,10+Transfer._pos)]);
            console.log('pos: ',Transfer._pos, ' ', sp);
            Transfer._pos += 10;
            Transfer._start = false;
            this._value = sp;
            //Transfer._updateValueCallback(sp);
        } else if(Transfer._pos!=0){
            Transfer._pos = 0;
            console.log('pos: ',Transfer._pos);
            //Transfer._updateValueCallback(new Buffer(0));
            this._value = new Buffer(0);
        }
  
  
  callback(this.RESULT_SUCCESS, this._value);
};

EchoCharacteristic.prototype.onWriteRequest = function(data, offset, withoutResponse, callback) {
  this._value = data;

  console.log('EchoCharacteristic - onWriteRequest: value = ' + this._value.toString('hex'));

  if (this._updateValueCallback) {
    console.log('EchoCharacteristic - onWriteRequest: notifying');

    //this._updateValueCallback(this._value);
	{
		Transfer._doc = new Buffer(JSON.stringify(require("../mm-resolved.json")),"ascii");
		Transfer._pos = 0;
		Transfer._updateValueCallback = this._updateValueCallback;
		Transfer._start = true;
        //Transfer.transfer();
	}
	
  }

  callback(this.RESULT_SUCCESS);
};

EchoCharacteristic.prototype.onNotify = function() {
    console.log('EchoCharacteristic - onNotify');
    console.log('pos: ',Transfer._pos, 'len ', Transfer._doc.length);
    //setTimeout(Transfer.transfer,50);
    /*
	if(this._pos<this._doc.length && this._pos!=0) {
		var locBuff = new Buffer(2);locBuff.writeUIntLE(this._pos/10,0,2);
		var sp = Buffer.concat([ locBuff  , this._doc.slice(this._pos,10+this._pos)]);
		console.log('pos: ',this._pos, ' ', sp);
		this._pos += 10;
		this._updateValueCallback(sp);
	} else if(this._pos!=0){
		this._pos = 0;
		console.log('pos: ',this._pos);
		this._updateValueCallback(new Buffer(0));

	} */
}

EchoCharacteristic.prototype.onSubscribe = function(maxValueSize, updateValueCallback) {
  console.log('EchoCharacteristic - onSubscribe ', maxValueSize, ' ',updateValueCallback);

  this._updateValueCallback = updateValueCallback;
};

EchoCharacteristic.prototype.onUnsubscribe = function() {
  console.log('EchoCharacteristic - onUnsubscribe');

  this._updateValueCallback = null;
};

module.exports = EchoCharacteristic;
