use std::future::{ready, Ready};
use actix_web::http::header;
use actix_web::{
    dev::{forward_ready, Service, ServiceRequest, ServiceResponse, Transform},
    Error,
};
use futures_util::future::LocalBoxFuture;

// There are two steps in middleware processing.
// 1. Middleware initialization, middleware factory gets called with
//    next service in chain as parameter.
// 2. Middleware's call method gets called with normal request.
pub struct SayHi;

// Middleware factory is `Transform` trait
// `S` - type of the next service
// `B` - type of response's body
impl<S, B> Transform<S, ServiceRequest> for SayHi
    where
        S: Service<ServiceRequest, Response = ServiceResponse<B>, Error = Error>,
        S::Future: 'static,
        B: 'static,
{
    type Response = ServiceResponse<B>;
    type Error = Error;
    type InitError = ();
    type Transform = SayHiMiddleware<S>;
    type Future = Ready<Result<Self::Transform, Self::InitError>>;

    fn new_transform(&self, service: S) -> Self::Future {
        ready(Ok(SayHiMiddleware { service }))
    }
}

pub struct SayHiMiddleware<S> {
    service: S,
}

impl<S, B> Service<ServiceRequest> for SayHiMiddleware<S>
    where
        S: Service<ServiceRequest, Response = ServiceResponse<B>, Error = Error>,
        S::Future: 'static,
        B: 'static,
{
    type Response = ServiceResponse<B>;
    type Error = Error;
    type Future = LocalBoxFuture<'static, Result<Self::Response, Self::Error>>;

    forward_ready!(service);

    fn call(&self, req: ServiceRequest) -> Self::Future {
        println!("Hi from start. You requested: {}", req.path());
        let content_type = req.headers().get(header::CONTENT_TYPE);
        let content_length = req.headers().get("Content-Length");
        println!("Content-Length->{:?}", req.headers().get("Content-Length"));
        println!("Content-Type->{:?}", req.headers().get("Content-Type"));

        if let Some(content_type) = content_type {
            if content_type.to_str().unwrap_or_default().starts_with("application/json") {
                // 如果是JSON，继续处理请求
                if let Some(content_length) = content_length {
                    if content_length.to_str().unwrap_or_default().parse::<usize>().unwrap_or(0) > 2 {
                        // return self.service.call(req);
                        // return self.service.call(req);

                    }

                    // } else {
                    //     let res = req.into_response(HttpResponse::BadRequest().finish().into_body());
                    //     return Box::pin(async { Ok(res) });
                }
            }
        }
        let fut = self.service.call(req);

        Box::pin(async move {
            let res = fut.await?;

            println!("Hi from response");
            Ok(res)
        })
    }
}
