var YST = new Object();

YST.Config = {
  debug : false,

  alertErrors : false,

  setAlertErrors : function(show) {
    YST.Config.alertErrors = (show==true);
  },

  showProcessingBox : true,

  setShowProcessingBox : function(show) {
    YST.Config.showProcessingBox =  (show==true);
  },

  hideBodyOnProcess : false,

  setHideBodyOnProcess : function(hide) {
    YST.Config.hideBodyOnProcess = (hide==true);
  },

  showProcessingTime : false,

  setShowProcessingTime : function(show) {
    YST.Config.showProcessingTime = (show==true);
  },

  allowMultiSet : false
}

YST.Util = {

  isUndefined:function (name, target) {
    if (name == null) return true;
    if (target == null) {
      return eval("(typeof "+name+" == 'undefined')");
    } else {
      return (typeof target[name] == 'undefined');
    }
  },

  isEmptyArray:function (name, target) {
    if (name == null) return true;
    if (target == null) {
      return eval("(typeof "+name+" == 'undefined' || "+name+" == null || !("+name+" instanceof Array) || "+name+".length == 0)");
    } else {
      return (typeof target[name] == 'undefined' || target[name] == null || !(target[name] instanceof Array) || target[name].length == 0);
    }
  }

}



YST.Debug = {
  errorToString:function (excep) {
    if (excep.message)
      return excep.message;
    else if (excep.description)
      return excep.description;
    else
      return ''+excep;
  },

  printTemplate:function (template, pre) {
    var result = pre+'[ ';
    if (template.length>1) {
      result += YST.Debug.printValue(template[0],'',',\n');
      for (var i = 1; i<template.length-1;i++) {
        result += YST.Debug.printValue(template[i],pre+'  ',',\n');
      }
      result += YST.Debug.printValue(template[template.length-1],pre+'  ','');
    } else if (template.length>0) {
      result += YST.Debug.printValue(template[0],'','');
    }
    result += '\n'+pre+']';
    return result;
  },

  printValue:function (template, pre, post) {
    var result = '';
    if (template == null) {
      result += pre+'\'null\''+post;
    } else if (typeof template == 'function') {
      if (template == YST.Txt.value)
        result += pre+'YST.Txt.value'+post;
      else if (template == YST.Txt.apply)
        result += pre+'YST.Txt.apply'+post;
      else if (template == YST.Txt.select)
        result += pre+'YST.Txt.select'+post;
      else if (template == YST.Txt.include)
        result += pre+'YST.Txt.include'+post;
      else if (template == YST.Txt.iff)
        result += pre+'YST.Txt.iff'+post;
      else
        result += pre+'function,\n'+post;
    } else if (typeof template == 'string') {
      result += pre+'\''+template+'\''+post;
    } else if (template.length){
      result += YST.Debug.printTemplate(template,pre)+post;
    }
    return result;
  }
}



YST.Aux = {
  emptyArray:["undefined"],

  getValuesLength:function (values) {
    var nValues = 0;
    for (var i=0;i<values.length;i++) {
      nValues = Math.max(nValues, values[i].length);
    }
    return nValues;
  },

  processEntities : function (str) {
    str = str.replace(/&quot;/g,'"');
    str = str.replace(/&amp;/g,"&");
    str = str.replace(/&lt;/g,"<");
    str = str.replace(/&gt;/g,">");
    return str;
  },

  insertEntities : function (str) {
    if (typeof str != 'string') return str;
    str = str.replace(/</g,'&lt;');
    str = str.replace(/"/g,'&quot;');
    return str;
  },

  fillArray : function (n) {
    var array = [];
    for (var i =0; i<n; i++)
      array[array.length] = i;
    return array;
  }
}

YST.txtProcessing = false;

YST.Txt = {
  value : function (contextValues, contextI, params, aux, template) {
    try {
      if (aux != null) YST.Txt.processTemplate(contextValues, contextI, params, [aux])

      return YST.Txt.processTemplate(contextValues, contextI, params, template);
    } catch (excep) {
      if (YST.Config.alertErrors) {
        return "<p><b>"+"Error processing template. \n  Error message: "+YST.Debug.errorToString(excep)+
                      "\n  Erroneous template: \n"+YST.Debug.printTemplate(template, '  ')+"</b></p>";
      } else throw excep;
    }
  },

  ystBool : function (values, i, params,  __ystbool) {
    var result = '';
    try {
      if (values.length != 0) {
        if (YST.Config.allowMultiSet) {  // 300408
          for (var _____j =0; _____j<values.length; _____j++) {
            eval('var values'+_____j+' = values['+_____j+']');
            eval('var e'+_____j+' = values'+_____j+'['+i+']');
          }
        }
        var values = values[0];
        var e = values[i];
      }

      eval('var __attrObj = '+__ystbool);

      for (var ___n in __attrObj) {
        var ___nlc = ___n.toLowerCase();
        var ___t = __attrObj[___n];

        if (___t) {
          result += ___nlc+'="'+___nlc+'" ';
        }
      }
    } catch (__excp) {
      var ___err = new Error();
      ___err.message = 'Error evaluating ystBool attribute ('+__ystbool+') '+YST.Debug.errorToString(__excp);
      throw ___err;
    }
    return result;
  },

  literal : function (contextValues, contextI, params, aux, template) {  // 160408 (aniadido aux)
    try {
      // evaluo el ystAux
      if (aux != null) YST.Txt.processTemplate(contextValues, contextI, params, [aux]); // 160408

      return YST.Txt.processTemplate(contextValues, contextI, params, template, true);
    } catch (excep) {
      if (YST.Config.alertErrors) {
        return "<p><b>"+"Error processing template. \n  Error message: "+YST.Debug.errorToString(excep)+
        "\n  Erroneous template: \n"+YST.Debug.printTemplate(template, '  ')+"</b></p>";
      } else throw excep;
    }
  },

  apply : function (contextValues, contextI, params, aux, valuesTxt, template) {
    var values = new Array();
    try {
      values = YST.Txt.evalValues(contextValues, contextI, params, valuesTxt);
    } catch  (excep) {
      if (YST.Config.alertErrors) {
        _____result = "<p><b>"+"Error processing set attribute. \n  Error message: "+YST.Debug.errorToString(excep)+
                      "\n  Erroneous element: \n"+YST.Debug.printTemplate(template, '  ')+"</b></p>";
      } else throw excep;
    }

    var s = '';

    if (values.length > 0) {
      var nValues = YST.Aux.getValuesLength(values);

      for (var i = 0; i < nValues; i++) {
        if (aux != null) YST.Txt.processTemplate(values, i, params, [aux])
        try {
          s+=YST.Txt.processTemplate(values, i, params, template);
        } catch (excep) {
          if (YST.Config.alertErrors) {
            _____result = "<p><b>"+"Error processing template. \n  Error message: "+YST.Debug.errorToString(excep)+
                          "\n  Erroneous template: \n"+YST.Debug.printTemplate(template, '  ')+"</b></p>";
          } else throw excep;
        }
      }
    }
    return s;
  },

  select : function (_____contextValues, _____contextI, params, _____valuesTxt, _____condition, ___aux, _____template) {
    var _____newValues = new Array();
    try {
      _____newValues = YST.Txt.evalValues(_____contextValues, _____contextI, params, _____valuesTxt);
    } catch  (excep) {
      if (YST.Config.alertErrors) {
        _____result = "<p><b>"+"Error processing set attribute. \n  Error message: "+YST.Debug.errorToString(excep)+"\n  Erroneous element: \n"+YST.Debug.printTemplate(_____template, "  ")+"</b></p>";
      } else throw excep;
    }

    var _____result = '';

    if (_____newValues.length > 0) {
      var nValues = YST.Aux.getValuesLength(_____newValues);
      for (var _____k = 0; _____k < nValues; _____k++) {
        var i = _____k
        if (_____valuesTxt=='YST.Aux.emptyArray' && _____contextValues.length>0) {
          _____newValues = _____contextValues;
          i =  _____contextI;
        }

        if (_____newValues.length != 0) {
          if (YST.Config.allowMultiSet) {
            for (var _____j =0; _____j<_____newValues.length; _____j++) {
              eval('var values'+_____j+' = _____newValues['+_____j+']')
              eval('var e'+_____j+' =values'+_____j+'['+i+']')
            }
          }
          var values = _____newValues[0];
          var e = values[i];
        }

        var _____conditionIndex = 4;
        while (_____conditionIndex < arguments.length) {
          var ___laux = arguments[_____conditionIndex+1];
          if (___laux != null) YST.Txt.processTemplate(_____newValues, i, params, [___laux])
          try {
            if (eval(arguments[_____conditionIndex])) {
              _____result+=YST.Txt.processTemplate(_____newValues, i, params, arguments[_____conditionIndex+2]);
            }
          } catch (excep) {
            if (YST.Config.alertErrors) {
              _____result = "<p><b>"+"Error processing conditional element. \n  Error message: "+YST.Debug.errorToString(excep)+
                            "\n  Erroneous element: \n"+YST.Debug.printTemplate(_____template, '  ')+
                            "\n  Set index: "+i+"</b></p>";
            } else throw excep;
          }
          _____conditionIndex += 3;
        }
      }
    }
    return _____result;
  },

  iff : function (contextValues, contextI, params, aux, condition, template) {
    return YST.Txt.select(contextValues, contextI, params, 'YST.Aux.emptyArray', condition, aux, template);
  },

  include : function (_____contextValues, _____contextI, params, ___aux, _____target, _____actualParamsTxt) {
    if (_____contextValues.length != 0) {
      var i = _____contextI;
      if (YST.Config.allowMultiSet) {
        for (var _____j =0; _____j<_____contextValues.length; _____j++) {
          eval('var values'+_____j+' = _____contextValues['+_____j+']')
          eval('var e'+_____j+' =values'+_____j+'['+i+']')
        }
      }
      var values = _____contextValues[0];
      var e = values[i];
    }

    var _____actualParams = new Object();
    if (_____actualParamsTxt != null && _____actualParamsTxt !='') {
      if (___aux != null) YST.Txt.processTemplate(_____contextValues, _____contextI, params, [___aux])

      _____actualParamsTxt = YST.Txt.processTemplate(_____contextValues, _____contextI, params, [_____actualParamsTxt]);
      try {
        eval('var _____actualParams = '+_____actualParamsTxt);
      } catch (excep) {
        if (YST.Config.alertErrors) {
          _____result = "<p><b>"+"Error processing params attribute. \n  Error message: "+YST.Debug.errorToString(excep)+"\n  Erroneous params attribute: "+_____actualParamsTxt+"</b></p>";
        } else throw excep;
      }
    }

    var _____result;
    if (_____target != null && _____target != '') {
      try {
        eval('_____result = '+_____target+'(_____contextValues, _____contextI, _____actualParams)');
      } catch (excep) {
        if (YST.Config.alertErrors) {
          _____result = "<p><b>"+"Error including Yeast template. \n  Error message: "+YST.Debug.errorToString(excep)+"</b></p>";
        } else throw excep;
      }
    } else {
      _____result = '';
    }

    return _____result;
  },

  evalValues : function (_____contextValues, _____contextI, params, _____valuesTxt) {
    if (_____contextValues.length != 0) {
      var i = _____contextI;
      if (YST.Config.allowMultiSet) {
        for (var _____j =0; _____j<_____contextValues.length; _____j++) {
          eval('var values'+_____j+' = _____contextValues['+_____j+']')
          eval('var e'+_____j+' =values'+_____j+'['+i+']')
        }
      }
      var values = _____contextValues[0];
      var e = values[i];
    }

    var _____values = new Array();
    if (typeof _____valuesTxt != 'undefined' && _____valuesTxt != null && _____valuesTxt != '') {
      if (YST.Config.allowMultiSet) {
        var _____valuesParts = _____valuesTxt.split(' ');
      } else {
        var _____valuesParts = [_____valuesTxt];
      }
      for (var _____j =0; _____j<_____valuesParts.length; _____j++) {
        try {
          _____values[_____j] = eval(_____valuesParts[_____j]);
        } catch (excep) {
          var ___err = new Error();
          ___err.message = 'Error evaluating ystSet attribute (\''+_____valuesParts[_____j]+ '\'): '+YST.Debug.errorToString(excep);
          throw ___err;
        }

        if (typeof _____values[_____j] == 'undefined'){
          var ___err = new Error();
          ___err.message = 'Undefined expression: '+_____valuesParts[_____j]+ ' in set attribute';
          throw ___err;
        }

        if (typeof _____values[_____j] == 'number') {
          _____values[_____j] = YST.Aux.fillArray(_____values[_____j]);
        }
      }
    }
    return _____values;
  },

  processTemplate : function (_____valuesToProcess, i, params, _____template, _____literal) {
    if (_____valuesToProcess.length != 0) {
      if (YST.Config.allowMultiSet) {
        for (var _____j =0; _____j<_____valuesToProcess.length; _____j++) {
          eval('var values'+_____j+' = _____valuesToProcess['+_____j+']')
          eval('var e'+_____j+' = values'+_____j+'['+i+']')
        }
      }
      var values = _____valuesToProcess[0];
      var e = values[i];
    }

    var ___s = '';

    for (var ___t=0;___t<_____template.length;___t++) {
      var ___templ = _____template[___t];

      if (typeof ___templ == 'function') {
        var ___func = ___templ;
        ___t ++;

        var ___params = _____template[___t];

        var ___invParams = [_____valuesToProcess, i, params];
        for (var ___k=0; ___k<___params.length;___k++)
          ___invParams[___invParams.length] = ___params[___k];
        ___s+= ___func.apply(window,___invParams);
      } else {
        var ___pos = 0;
        while (___pos < ___templ.length) {
          if (___templ.charAt(___pos) == '\\') {
            ___pos++;
            if (___templ.charAt(___pos) != '$')
              ___s+='\\';

            ___s+=___templ.charAt(___pos);
          } else if (___templ.charAt(___pos) == '$') {
            ___pos++;
            var ___writeValue = true;
            if (___templ.charAt(___pos) == '#') {
              ___writeValue = false;
              ___pos++;
            }
            var ___expr = "";
            while (___templ.charAt(___pos) != '$' && ___pos < ___templ.length)
              ___expr += ___templ.charAt(___pos++);

            try {
              if(___pos == ___templ.length) {
                      var ___err = new Error();
                ___err.message = 'Unbalanced $ in expression: $'+___expr;
                throw ___err;
              }

              ___expr = YST.Aux.processEntities(___expr);

              var ___exprValue = eval(___expr);

              if (typeof ___exprValue == 'undefined' || ___exprValue == 'undefined' && values==YST.Aux.emptyArray){
                var ___err = new Error();
                ___err.message = 'Undefined expression: '+___expr;
                throw ___err;
              }

              if (___writeValue && ___exprValue != null) {
                if (!_____literal) {
                  ___s+= YST.Aux.insertEntities(___exprValue);
                } else {
                  ___s+= ___exprValue;
                }
              }
            } catch (excep) {
              var msg = YST.Debug.errorToString(excep);
              if (___writeValue) ___s += "[YST_Error! - "+msg+"]";
            }
          } else
            ___s+=___templ.charAt(___pos);

          ___pos++;
        }
      }
    }
    return ___s;
  }
}
