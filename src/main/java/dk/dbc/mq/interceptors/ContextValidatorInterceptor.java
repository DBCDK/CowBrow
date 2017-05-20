package dk.dbc.mq.interceptors;

import dk.dbc.mq.ejb.ContextBean;
import dk.dbc.mq.json.JsonHandler;
import dk.dbc.mq.json.ResultJSON;
import dk.dbc.mq.json.StatusJson;

import javax.interceptor.AroundInvoke;
import javax.interceptor.Interceptor;
import javax.interceptor.InvocationContext;
import java.io.Serializable;

@Interceptor
@ContextValidator
public class ContextValidatorInterceptor implements Serializable {
    @AroundInvoke
    public Object validate(InvocationContext invocationContext) throws Exception {
        ContextBean bean = (ContextBean) invocationContext.getTarget();
        if(bean.getContext() == null || bean.getSession() == null) {
            ResultJSON<StatusJson> result = new ResultJSON<>();
            result.withResponseCode(500).withResponseType(ResultJSON.TYPE_STATUS);
            result.addResponse(new StatusJson().withMessage("Couldn't get JMS session. Try logging in first."));
            return JsonHandler.toJson(result);
        }
        return invocationContext.proceed();
    }
}
