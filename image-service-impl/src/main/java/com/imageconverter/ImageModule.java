package com.imageconverter;

import com.app.IImageService;
import com.google.inject.AbstractModule;

public class ImageModule extends AbstractModule {
    @Override
    protected void configure() {
        bind(IImageService.class).to(ImageServiceImpl.class);
    }
}